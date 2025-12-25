package com.aynu.auth.service;

import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.auth.domain.dto.LoginAdminFormDTO;
import com.aynu.auth.domain.po.AuthTokens;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 认证令牌表，存储用户的访问令牌和刷新令牌信息（时间字段为毫秒级时间戳） 服务类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
public interface IAuthTokensService extends IService<AuthTokens> {

    String login(LoginFormDTO loginFormDTO, boolean b);

    void logout();

    String refreshToken(String decode);

    String loginAdmin(LoginAdminFormDTO dto);
}
