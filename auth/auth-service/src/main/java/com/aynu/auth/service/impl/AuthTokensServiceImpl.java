package com.aynu.auth.service.impl;

import com.aynu.api.client.user.UserClient;
import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.auth.common.constants.JwtConstants;
import com.aynu.auth.domain.dto.LoginAdminFormDTO;
import com.aynu.auth.domain.po.AuthTokens;
import com.aynu.auth.mapper.AuthTokensMapper;
import com.aynu.auth.service.IAuthTokensService;
import com.aynu.auth.util.JwtTool;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.BooleanUtils;
import com.aynu.common.utils.WebUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 认证令牌表，存储用户的访问令牌和刷新令牌信息（时间字段为毫秒级时间戳） 服务实现类
 * </p>
 *
 * @author xiaozhou
 * @since 2025-12-18
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthTokensServiceImpl extends ServiceImpl<AuthTokensMapper, AuthTokens> implements IAuthTokensService {

    private final JwtTool jwtTool;
    private final UserClient userClient;

    @Override
    public String login(LoginFormDTO loginFormDTO, boolean isStaff) {
        // 1.查询并校验用户信息
        LoginUserDTO detail = userClient.queryUserDetail(loginFormDTO, isStaff);
        if (detail == null) {
            throw new BadRequestException("登录信息有误");
        }

        // 2.基于JWT生成登录token
        // 2.1.设置记住我标记
        detail.setRememberMe(loginFormDTO.getRememberMe());

        return generateToken(detail);
    }

    @Override
    public String loginAdmin(LoginAdminFormDTO dto) {

        // 1.查询并校验用户信息
        if (!"admin".equals(dto.getUsername())) {
            throw new BadRequestException("用户名错误");
        }
        if (!"passwd".equals(dto.getPassword())) {
            throw new BadRequestException("密码错误");
        }

        LoginUserDTO detail = new LoginUserDTO();
        detail.setUserId(1L);
        detail.setRoleId(1L);
        // 2.基于JWT生成登录token
        // 2.1.设置记住我标记
        detail.setRememberMe(dto.getRememberMe());

        return generateToken(detail);
    }

    private String generateToken(LoginUserDTO detail) {
        // 2.2.生成access-token
        String token = jwtTool.createToken(detail);
        // 2.3.生成refresh-token，将refresh-token的JTI 保存到Redis
        String refreshToken = jwtTool.createRefreshToken(detail);
        // 2.4.将refresh-token写入用户cookie，并设置HttpOnly为true
        int maxAge = BooleanUtils.isTrue(detail.getRememberMe()) ? (int) JwtConstants.JWT_REMEMBER_ME_TTL.toSeconds() : -1;
        WebUtils.cookieBuilder()
                .name(detail.getRoleId() == 2 ? JwtConstants.REFRESH_HEADER : JwtConstants.ADMIN_REFRESH_HEADER)
                .value(refreshToken)
                .maxAge(maxAge)
                .httpOnly(true)
                .build();
        return token;
    }

    @Override
    public void logout() {
        // 删除jti
        jwtTool.cleanJtiCache();
        // 删除客户端refresh cookie
        WebUtils.cookieBuilder().name(JwtConstants.REFRESH_HEADER).value("").maxAge(0).httpOnly(true).build();
        // 删除管理端refresh cookie
        WebUtils.cookieBuilder().name(JwtConstants.ADMIN_REFRESH_HEADER).value("").maxAge(0).httpOnly(true).build();
    }

    @Override
    public String refreshToken(String refreshToken) {
        // 1.校验refresh-token,校验JTI
        LoginUserDTO userDTO = jwtTool.parseRefreshToken(refreshToken);
        // 2.生成新的access-token、refresh-token
        return generateToken(userDTO);
    }


}
