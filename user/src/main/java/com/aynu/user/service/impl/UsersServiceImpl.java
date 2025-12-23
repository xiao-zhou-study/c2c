package com.aynu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.client.storage.FileClient;
import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.UserContext;
import com.aynu.user.domain.dto.PasswordChangeDTO;
import com.aynu.user.domain.dto.UserProfileDTO;
import com.aynu.user.domain.dto.VerifyDTO;
import com.aynu.user.domain.po.UserProfiles;
import com.aynu.user.domain.po.UserStats;
import com.aynu.user.domain.po.Users;
import com.aynu.user.domain.vo.UserProfileVO;
import com.aynu.user.domain.vo.UserStatsVO;
import com.aynu.user.mapper.UsersMapper;
import com.aynu.user.service.IUserProfilesService;
import com.aynu.user.service.IUserStatsService;
import com.aynu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.aynu.common.utils.AvatarUtils.getRandomAvatar;

/**
 * <p>
 * 用户基本信息表，存储用户核心登录和基础信息 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UsersServiceImpl extends ServiceImpl<UsersMapper, Users> implements IUsersService {

    private final IUserProfilesService userProfilesService;
    private final IUserStatsService userStatsService;
    private final FileClient fileClient;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void saveUser(UserDTO userDTO) {
        String studentId = userDTO.getStudentId();
        Users user = lambdaQuery().eq(Users::getStudentId, studentId).one();
        if (user != null) {
            throw new BadRequestException("用户已存在");
        }

        String password = userDTO.getPassword();
        password = passwordEncoder.encode(password);
        Users users = new Users().setStudentId(studentId)
                .setUsername(userDTO.getUsername())
                .setDepartment(userDTO.getDepartment())
                .setEmail(userDTO.getEmail())
                .setPasswordHash(password);
        setDefaultUserInfo(users);

        save(users);
    }

    @Override
    public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff) {
        String username = loginDTO.getUsername();
        String email = loginDTO.getEmail();
        String studentId = loginDTO.getStudentId();
        String password = loginDTO.getPassword();

        if (StrUtil.isBlank(password)) {
            throw new BadRequestException("密码不能为空");
        }

        Users user;
        if (isStaff) {
            // 管理员登录，只支持用户名+密码
            if (StrUtil.isBlank(username)) {
                throw new BadRequestException("用户名不能为空");
            }
            user = lambdaQuery().eq(Users::getUsername, username).one();
        } else {
            // 普通用户登录，支持邮箱或学号
            if (StrUtil.isNotBlank(email)) {
                // 使用邮箱登录
                user = lambdaQuery().eq(Users::getEmail, email).one();
            } else if (StrUtil.isNotBlank(studentId)) {
                // 使用学号登录
                user = lambdaQuery().eq(Users::getStudentId, studentId).one();
            } else {
                throw new BadRequestException("邮箱或学号不能为空");
            }
        }

        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("密码错误");
        }

        // 构造返回的LoginUserDTO
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setUserId(user.getId());
        // 根据是否是管理员设置角色ID
        loginUserDTO.setRoleId(isStaff ? 2L : 1L); // 管理员角色ID为2，普通用户为1
        loginUserDTO.setRememberMe(loginDTO.getRememberMe());

        return loginUserDTO;
    }

    @Override
    public List<UserDTO> queryUserByIds(Iterable<Long> ids) {
        List<Users> list = lambdaQuery().in(Users::getId, ids).eq(Users::getStatus, 1).list();
        return list.stream().map(Users::toDTO).toList();
    }

    @Override
    public void addStaff(UserDTO userDTO) {
        Users users = BeanUtil.toBean(userDTO, Users.class);
        String password = userDTO.getPassword();
        password = passwordEncoder.encode(password);
        users.setPasswordHash(password);
        setDefaultUserInfo(users);
        save(users);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        Users user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }
        return user.toDTO();
    }

    @Override
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        Users user = getById(userId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 只更新允许修改的字段
        if (StrUtil.isNotBlank(userDTO.getNickname())) {
            user.setNickname(userDTO.getNickname());
        }
        if (StrUtil.isNotBlank(userDTO.getPhone())) {
            user.setPhone(userDTO.getPhone());
        }
        if (StrUtil.isNotBlank(userDTO.getAvatarUrl())) {
            user.setAvatarUrl(userDTO.getAvatarUrl());
        }
        if (StrUtil.isNotBlank(userDTO.getSchool())) {
            user.setSchool(userDTO.getSchool());
        }
        if (StrUtil.isNotBlank(userDTO.getGrade())) {
            user.setGrade(userDTO.getGrade());
        }

        user.setUpdatedAt(System.currentTimeMillis());
        updateById(user);
        return user.toDTO();
    }

    @Override
    public UserProfileVO getUserProfile(Long userId) {
        UserProfiles profile = userProfilesService.lambdaQuery().eq(UserProfiles::getUserId, userId).one();
        if (profile == null) {
            // 如果不存在资料，返回空的资料对象
            return new UserProfileVO();
        }
        return BeanUtil.toBean(profile, UserProfileVO.class);
    }

    @Override
    @Transactional
    public UserProfileVO updateUserProfile(Long userId, UserProfileDTO profileDTO) {
        UserProfiles profile = userProfilesService.lambdaQuery().eq(UserProfiles::getUserId, userId).one();

        long now = System.currentTimeMillis();

        if (profile == null) {
            // 创建新资料
            profile = BeanUtil.toBean(profileDTO, UserProfiles.class);
            profile.setUserId(userId);
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            userProfilesService.save(profile);
        } else {
            // 更新现有资料
            if (profileDTO.getRealName() != null) {
                profile.setRealName(profileDTO.getRealName());
            }
            if (profileDTO.getGender() != null) {
                profile.setGender(profileDTO.getGender());
            }
            if (profileDTO.getBirthday() != null) {
                profile.setBirthday(profileDTO.getBirthday());
            }
            if (profileDTO.getBio() != null) {
                profile.setBio(profileDTO.getBio());
            }
            if (profileDTO.getCampus() != null) {
                profile.setCampus(profileDTO.getCampus());
            }
            if (profileDTO.getDormitory() != null) {
                profile.setDormitory(profileDTO.getDormitory());
            }
            if (profileDTO.getQq() != null) {
                profile.setQq(profileDTO.getQq());
            }
            if (profileDTO.getWechat() != null) {
                profile.setWechat(profileDTO.getWechat());
            }
            profile.setUpdatedAt(now);
            userProfilesService.updateById(profile);
        }

        return BeanUtil.toBean(profile, UserProfileVO.class);
    }

    @Override
    public UserStatsVO getUserStats(Long userId) {
        UserStats stats = userStatsService.lambdaQuery().eq(UserStats::getUserId, userId).one();
        if (stats == null) {
            // 如果不存在统计信息，返回空的统计对象
            return new UserStatsVO();
        }
        return BeanUtil.toBean(stats, UserStatsVO.class);
    }

    @Override
    @Transactional
    public void changePassword(PasswordChangeDTO passwordChangeDTO) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("未登录或登录已过期");
        }

        Users user = getById(currentUserId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(passwordChangeDTO.getOldPassword(), user.getPasswordHash())) {
            throw new BadRequestException("原密码不正确");
        }

        // 更新密码
        String newPassword = passwordEncoder.encode(passwordChangeDTO.getNewPassword());
        user.setPasswordHash(newPassword);
        user.setUpdatedAt(System.currentTimeMillis());
        updateById(user);
    }

    @Override
    @Transactional
    public void verifyUser(VerifyDTO verifyDTO) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("未登录或登录已过期");
        }

        Users user = getById(currentUserId);
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 检查是否已认证
        if (user.getIsVerified()) {
            throw new BadRequestException("用户已完成实名认证");
        }

        // 更新用户资料中的真实姓名
        UserProfiles profile = userProfilesService.lambdaQuery().eq(UserProfiles::getUserId, currentUserId).one();

        long now = System.currentTimeMillis();
        if (profile == null) {
            profile = new UserProfiles();
            profile.setUserId(currentUserId);
            profile.setRealName(verifyDTO.getRealName());
            profile.setCreatedAt(now);
            profile.setUpdatedAt(now);
            userProfilesService.save(profile);
        } else {
            profile.setRealName(verifyDTO.getRealName());
            profile.setUpdatedAt(now);
            userProfilesService.updateById(profile);
        }

        // 更新用户认证状态
        user.setIsVerified(true);
        user.setUpdatedAt(now);
        updateById(user);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        Long currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new BadRequestException("未登录或登录已过期");
        }

        // 调用文件服务上传图片
        String url = fileClient.uploadFile(file, "user");

        // 更新用户头像
        Users user = getById(currentUserId);
        user.setAvatarUrl(url);
        user.setUpdatedAt(System.currentTimeMillis());
        updateById(user);

        return url;
    }

    private void setDefaultUserInfo(Users users) {
        users.setCreditScore(100);
        users.setAvatarUrl(getRandomAvatar());
        users.setIsVerified(false);
        users.setStatus(1);
        users.setCreatedAt(System.currentTimeMillis());
        users.setUpdatedAt(System.currentTimeMillis());
    }

    private Long getCurrentUserId() {
        return UserContext.getUser();
    }

}
