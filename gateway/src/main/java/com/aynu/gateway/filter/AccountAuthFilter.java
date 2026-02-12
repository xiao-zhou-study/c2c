package com.aynu.gateway.filter;

import com.aynu.authsdk.gateway.util.AuthUtil;
import com.aynu.common.domain.R;
import com.aynu.common.domain.dto.LoginUserDTO;
import com.aynu.gateway.config.AuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

import static com.aynu.auth.common.constants.JwtConstants.AUTHORIZATION_HEADER;
import static com.aynu.auth.common.constants.JwtConstants.USER_HEADER;

@Slf4j

@Component
public class AccountAuthFilter implements GlobalFilter, Ordered {

    private final AuthUtil authUtil;
    private final AuthProperties authProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    public AccountAuthFilter(AuthUtil authUtil, AuthProperties authProperties) {
        this.authUtil = authUtil;
        this.authProperties = authProperties;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1.获取请求request信息
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethodValue();
        String path = request.getPath().toString();
        String antPath = method + ":" + path;
        String remoteAddr = request.getRemoteAddress() != null ? request.getRemoteAddress().getAddress().getHostAddress() : "unknown";

        log.info("=== 网关请求开始 === 方法:{}, 路径:{}, 远程IP:{}", method, path, remoteAddr);

        // 2.判断是否是无需登录的路径
        if (isExcludePath(antPath)) {
            log.info("白名单路径，放行: {}", antPath);
            // 直接放行
            return chain.filter(exchange);
        }

        // 3.尝试获取用户信息
        List<String> authHeaders = exchange.getRequest().getHeaders().get(AUTHORIZATION_HEADER);
        String token = authHeaders == null ? "" : authHeaders.get(0);
        log.debug("开始解析Token: {} (长度: {})", 
                token != null && token.length() > 20 ? token.substring(0, 20) + "..." : "null", 
                token != null ? token.length() : 0);

        R<LoginUserDTO> r = authUtil.parseToken(token);

        // 4.如果用户是登录状态，尝试更新请求头，传递用户信息
        if (r.success()) {
            Long userId = r.getData().getUserId();
            log.info("用户认证成功, userId: {}, 请求路径: {}", userId, path);
            exchange.mutate()
                    .request(builder -> builder.header(USER_HEADER, r.getData().getUserId().toString()))
                    .build();
        } else {
            log.warn("用户认证失败: {}, 错误: {}, 请求路径: {}", r.getCode(), r.getMsg(), path);
        }

        // 5.校验权限
        log.debug("开始校验权限: {}", antPath);
        authUtil.checkAuth(antPath, r);
        log.debug("权限校验通过");

        // 6.放行
        return chain.filter(exchange);
    }

    private boolean isExcludePath(String antPath) {
        for (String pathPattern : authProperties.getExcludePath()) {
            if (antPathMatcher.match(pathPattern, antPath)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int getOrder() {
        return 1000;
    }
}
