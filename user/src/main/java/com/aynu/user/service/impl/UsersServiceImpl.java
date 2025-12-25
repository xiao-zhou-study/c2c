package com.aynu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.client.auth.AuthClient;
import com.aynu.api.dto.auth.RoleDTO;
import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.autoconfigure.mq.RabbitMqHelper;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.AvatarUtils;
import com.aynu.common.utils.UserContext;
import com.aynu.user.domain.dto.PasswordChangeDTO;
import com.aynu.user.domain.dto.UserRegisterDTO;
import com.aynu.user.domain.dto.VerifyDTO;
import com.aynu.user.domain.po.UserProfiles;
import com.aynu.user.domain.po.UserStats;
import com.aynu.user.domain.po.Users;
import com.aynu.user.domain.vo.UserStatsVO;
import com.aynu.user.mapper.UsersMapper;
import com.aynu.user.service.IUserProfilesService;
import com.aynu.user.service.IUserStatsService;
import com.aynu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.aynu.common.constants.Constant.ROLE_GENERAL;
import static com.aynu.common.constants.MqConstants.Exchange.USER_EXCHANGE;
import static com.aynu.common.constants.MqConstants.Key.USER_NEW_KEY;

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
    private final AuthClient authClient;
    private final PasswordEncoder passwordEncoder;
    private final RabbitMqHelper rabbitMqHelper;
    private final UsersMapper userMapper;

    @Override
    public void saveUser(UserRegisterDTO dto) {
        String studentId = dto.getStudentId();
        Users user = lambdaQuery().eq(Users::getStudentId, studentId).one();
        if (user != null) {
            throw new BadRequestException("用户已存在");
        }

        String password = dto.getPassword();
        password = passwordEncoder.encode(password);
        Users users = new Users().setStudentId(studentId)
                .setUsername(dto.getUsername())
                .setSchool(dto.getSchool())
                .setDepartment(dto.getDepartment())
                .setGrade(dto.getGrade())
                .setEmail(dto.getEmail())
                .setPasswordHash(password);
        setDefaultUserInfo(users);

        save(users);
        UserDTO userDTO = BeanUtil.toBean(users, UserDTO.class);
        userDTO.setRole(ROLE_GENERAL);
        rabbitMqHelper.send(USER_EXCHANGE, USER_NEW_KEY, userDTO);
    }

    @Override
    public LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff) {
        String email = loginDTO.getEmail();
        String studentId = loginDTO.getStudentId();
        String password = loginDTO.getPassword();
        String phone = loginDTO.getPhone();

        if (StrUtil.isBlank(password)) {
            throw new BadRequestException("密码不能为空");
        }

        Users user;
        if (StrUtil.isNotBlank(email)) {
            // 使用邮箱登录
            user = lambdaQuery().eq(Users::getEmail, email).one();
        } else if (StrUtil.isNotBlank(studentId)) {
            // 使用学号登录
            user = lambdaQuery().eq(Users::getStudentId, studentId).one();
        } else if (StrUtil.isNotBlank(phone)) {
            // 使用手机号登录
            user = lambdaQuery().eq(Users::getPhone, phone).one();
        } else {
            throw new BadRequestException("邮箱、学号或手机号不能为空");
        }

        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadRequestException("密码错误");
        }

        // 获取用户角色Id
        RoleDTO roleDTO = authClient.queryRoleByUserId(user.getId());

        // 构造返回的LoginUserDTO
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setUserId(user.getId());
        // 根据是否是管理员设置角色ID
        loginUserDTO.setRoleId(roleDTO.getId());
        loginUserDTO.setRememberMe(loginDTO.getRememberMe());

        return loginUserDTO;
    }


    @Override
    public List<UserDTO> queryUserByIds(Iterable<Long> ids) {
        List<Users> users = lambdaQuery().in(Users::getId, ids).eq(Users::getStatus, 1).list();
        List<UserProfiles> userProfiles = userProfilesService.lambdaQuery().in(UserProfiles::getUserId, ids).list();

        if (userProfiles == null) {
            return users.stream().map(Users::toDTO).toList();
        }
        Map<Long, UserProfiles> userProfilesMap = userProfiles.stream()
                .collect(Collectors.toMap(UserProfiles::getUserId, Function.identity()));
        return users.stream().map(user -> {
            UserProfiles userProfiles1 = userProfilesMap.getOrDefault(user.getId(), new UserProfiles());
            return convertToUserDTO(user, userProfiles1);
        }).collect(Collectors.toList());
    }

    private UserDTO convertToUserDTO(Users user, UserProfiles userProfiles1) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setUsername(user.getUsername());
        userDTO.setEmail(user.getEmail());
        userDTO.setPhone(user.getPhone());
        userDTO.setPassword(user.getPasswordHash());
        userDTO.setAvatarUrl(user.getAvatarUrl());
        userDTO.setStudentId(user.getStudentId());
        userDTO.setSchool(user.getSchool());
        userDTO.setDepartment(user.getDepartment());
        userDTO.setGrade(user.getGrade());
        userDTO.setCreditScore(user.getCreditScore());
        userDTO.setIsVerified(user.getIsVerified());
        userDTO.setStatus(user.getStatus());
        userDTO.setLastLoginAt(user.getLastLoginAt());
        userDTO.setRealName(userProfiles1.getRealName());
        userDTO.setGender(userProfiles1.getGender());
        userDTO.setBirthday(userProfiles1.getBirthday());
        userDTO.setBio(userProfiles1.getBio());
        userDTO.setQq(userProfiles1.getQq());
        userDTO.setWechat(userProfiles1.getWechat());
        userDTO.setCreatedAt(user.getCreatedAt());
        userDTO.setUpdatedAt(user.getUpdatedAt());
        return userDTO;
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
        UserProfiles userProfiles = userProfilesService.lambdaQuery().eq(UserProfiles::getUserId, userId).one();
        if (userProfiles == null) {
            userProfiles = new UserProfiles();
        }
        return convertToUserDTO(user, userProfiles);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long userId, UserDTO userDTO) {
        Users user = getById(userId);
        UserProfiles userProfiles = userProfilesService.lambdaQuery().eq(UserProfiles::getUserId, userId).one();
        if (userProfiles == null) {
            userProfiles = new UserProfiles();
            userProfiles.setUserId(userId);
        }
        if (user == null) {
            throw new BadRequestException("用户不存在");
        }

        if (StrUtil.isNotBlank(userDTO.getUsername())) {
            user.setUsername(userDTO.getUsername());
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

        if (StrUtil.isNotBlank(userDTO.getDepartment())) {
            user.setDepartment(userDTO.getDepartment());
        }

        if (StrUtil.isNotBlank(userDTO.getGrade())) {
            user.setGrade(userDTO.getGrade());
        }

        if (userDTO.getStatus() != null) {
            user.setStatus(userDTO.getStatus());
        }

        if (userDTO.getGender() != null) {
            userProfiles.setGender(userDTO.getGender());
        }

        if (userDTO.getBirthday() != null) {
            userProfiles.setBirthday(userDTO.getBirthday());
        }

        if (StrUtil.isNotBlank(userDTO.getBio())) {
            userProfiles.setBio(userDTO.getBio());
        }

        if (StrUtil.isNotBlank(userDTO.getQq())) {
            userProfiles.setQq(userDTO.getQq());
        }

        if (StrUtil.isNotBlank(userDTO.getWechat())) {
            userProfiles.setWechat(userDTO.getWechat());
        }

        updateById(user);
        userProfilesService.saveOrUpdate(userProfiles);
        return convertToUserDTO(user, userProfiles);
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
    public PageDTO<UserDTO> queryUserPage(PageQuery query, String keyword, Integer status) {
        LambdaQueryChainWrapper<Users> wrapper = new LambdaQueryChainWrapper<>(userMapper);
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Users::getUsername, keyword)
                    .or()
                    .like(Users::getEmail, keyword)
                    .or()
                    .like(Users::getPhone, keyword));
        }

        if (status != null) {
            wrapper.eq(Users::getStatus, status);
        }

        Page<Users> page = wrapper.page(query.toMpPage("created_at", false));
        List<Users> records = page.getRecords();
        if (CollUtil.isEmpty(records)) {
            return PageDTO.empty(page);
        }
        Set<Long> userIds = records.stream().map(Users::getId).collect(Collectors.toSet());
        List<UserProfiles> userProfiles = userProfilesService.lambdaQuery().in(UserProfiles::getUserId, userIds).list();

        List<UserDTO> list;
        if (userProfiles == null) {
            list = records.stream().map(Users::toDTO).toList();
            return PageDTO.of(page, list);
        }
        Map<Long, UserProfiles> userProfilesMap = userProfiles.stream()
                .collect(Collectors.toMap(UserProfiles::getUserId, Function.identity()));
        list = records.stream().map(user -> {
            UserProfiles userProfiles1 = userProfilesMap.getOrDefault(user.getId(), new UserProfiles());
            return convertToUserDTO(user, userProfiles1);
        }).collect(Collectors.toList());
        return PageDTO.of(page, list);
    }

    private void setDefaultUserInfo(Users users) {
        users.setCreditScore(100);
        users.setAvatarUrl(AvatarUtils.getRandomAvatar());
        users.setIsVerified(false);
        users.setStatus(1);
    }

    private Long getCurrentUserId() {
        return UserContext.getUser();
    }

}

