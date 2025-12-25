package com.aynu.user.service;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.api.dto.user.UserDTO;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.domain.dto.PageDTO;
import com.aynu.common.domain.query.PageQuery;
import com.aynu.user.domain.dto.PasswordChangeDTO;
import com.aynu.user.domain.dto.UserRegisterDTO;
import com.aynu.user.domain.dto.VerifyDTO;
import com.aynu.user.domain.po.Users;
import com.aynu.user.domain.vo.UserStatsVO;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

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

    void saveUser(UserRegisterDTO dto);

    LoginUserDTO queryUserDetail(LoginFormDTO loginDTO, boolean isStaff);

    List<UserDTO> queryUserByIds(Iterable<Long> ids);

    void addStaff(UserDTO userDTO);

    UserDTO getUserById(Long userId);

    UserDTO updateUser(Long userId, UserDTO userDTO);

    UserStatsVO getUserStats(Long userId);

    void changePassword(PasswordChangeDTO passwordChangeDTO);

    void verifyUser(VerifyDTO verifyDTO);

    String uploadAvatar(MultipartFile file);

    PageDTO<UserDTO> queryUserPage(PageQuery query, String keyword, Integer status);
}