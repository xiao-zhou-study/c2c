package com.aynu.authsdk.resource.interceptors;

import com.aynu.auth.common.constants.JwtConstants;
import com.aynu.common.utils.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class UserInfoInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request,
                             @Nonnull HttpServletResponse response,
                             @Nonnull Object handler) {
        // 1.尝试获取头信息中的用户信息
        String authorization = request.getHeader(JwtConstants.USER_HEADER);
        // 2.判断是否为空
        if (authorization == null) {
            return true;
        }
        // 3.转为用户id并保存
        try {
            Long userId = Long.valueOf(authorization);
            UserContext.setUser(userId);
            return true;
        } catch (NumberFormatException e) {
            log.error("用户身份信息格式不正确，{}, 原因：{}", authorization, e.getMessage());
            return true;
        }
    }

    @Override
    public void afterCompletion(@Nonnull HttpServletRequest request,
                                @Nonnull HttpServletResponse response,
                                @Nonnull Object handler,
                                Exception ex) {
        // 清理用户信息
        UserContext.removeUser();
    }
}
