package com.aynu.user.service;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.user.domain.po.Users;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户基本信息表，存储用户核心登录和基础信息 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-16
 */
public interface IUsersService extends IService<Users> {

    void saveUser(UserDTO userDTO);

    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    List<UserDTO> queryUserByIds(Iterable<Long> ids);

    void addStaff(UserDTO userDTO);
}
