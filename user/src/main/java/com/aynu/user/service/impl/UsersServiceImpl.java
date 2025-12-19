package com.aynu.user.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.user.domain.po.Users;
import com.aynu.user.mapper.UsersMapper;
import com.aynu.user.service.IUserProfilesService;
import com.aynu.user.service.IUsersService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
        setDefaultUserInfo(users);
        save(users);
    }

    private void setDefaultUserInfo(Users users) {
        users.setCreditScore(100);
        users.setAvatarUrl(getRandomAvatar());
        users.setIsVerified(false);
        users.setStatus(1);
        users.setCreatedAt(System.currentTimeMillis());
        users.setUpdatedAt(System.currentTimeMillis());
    }

}
