package com.aynu.auth.controller;


import com.aynu.api.dto.user.LoginFormDTO;
import com.aynu.auth.common.constants.JwtConstants;
import com.aynu.auth.domain.dto.LoginAdminFormDTO;
import com.aynu.auth.service.IAuthTokensService;
import com.aynu.common.exceptions.BadRequestException;
import com.aynu.common.utils.WebUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 账户登录相关接口
 */
@RestController
@RequestMapping("/accounts")
@Api(tags = "账户管理")
@RequiredArgsConstructor
public class AuthTokensController {

    private final IAuthTokensService authTokensService;

    @ApiOperation("登录并获取token")
    @PostMapping(value = "/login")
    public String loginByPw(@RequestBody LoginFormDTO loginFormDTO) {
        return authTokensService.login(loginFormDTO, false);
    }

    @ApiOperation("管理端登录并获取token")
    @PostMapping(value = "/admin/login")
    public String adminLoginByPw(@RequestBody LoginAdminFormDTO dto) {
        return authTokensService.loginAdmin(dto);
    }

    @ApiOperation("退出登录")
    @PostMapping(value = "/logout")
    public void logout() {
        authTokensService.logout();
    }

    @ApiOperation("刷新token")
    @GetMapping(value = "/refresh")
    public String refreshToken(@CookieValue(value = JwtConstants.REFRESH_HEADER, required = false) String studentToken,
                               @CookieValue(value = JwtConstants.ADMIN_REFRESH_HEADER,
                                       required = false) String adminToken) {
        if (studentToken == null && adminToken == null) {
            throw new BadRequestException("用户未登录");
        }
        String clientType = WebUtils.getHeader("X-Client-Type");
        if (clientType == null) {
            throw new BadRequestException("Client-Type不能为空");
        }
        // 通过自定义请求头区分管理端和客户端：admin是管理端，client是客户端
        String token = "admin".equals(clientType) ? adminToken : studentToken;
        if (token == null) {
            throw new BadRequestException("用户未登录");
        }
        return authTokensService.refreshToken(WebUtils.cookieBuilder()
                .decode(token));
    }
}


