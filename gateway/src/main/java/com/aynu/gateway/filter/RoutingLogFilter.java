package com.aynu.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Component
public class RoutingLogFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();

        String method = request.getMethodValue();
        String path = request.getPath()
                .toString();
        String remoteAddr = request.getRemoteAddress() != null ? request.getRemoteAddress()
                .getAddress()
                .getHostAddress() : "unknown";

        log.info(">>> [网关路由] 开始处理请求");
        log.info("  方法: {}", method);
        log.info("  路径: {}", path);
        log.info("  远程IP: {}", remoteAddr);
        log.info("  完整路径: {}", request.getURI());

        // 获取匹配的路由信息
        Route route = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
        if (route != null) {
            log.info("  匹配路由ID: {}", route.getId());
            log.info("  目标服务: {}", route.getUri());
            log.info("  路由过滤器: {}", route.getFilters());
        } else {
            log.warn("  未匹配到任何路由配置!");
        }

        // 添加开始时间到 exchange 中，用于计算耗时
        exchange.getAttributes()
                .put("startTime", startTime);

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;

                    // 获取实际路由的目标地址
                    URI targetUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                    if (targetUri != null) {
                        log.info(">>> [网关路由] 请求完成，耗时: {}ms", duration);
                        log.info("  实际转发到: {}", targetUri);
                    } else {
                        log.warn(">>> [网关路由] 请求完成，但未获取到目标URL，耗时: {}ms", duration);
                    }

                    // 记录响应状态
                    exchange.getResponse();
                    int statusCode = exchange.getResponse()
                            .getStatusCode() != null ? exchange.getResponse()
                            .getStatusCode()
                            .value() : 0;
                    log.info("  响应状态码: {}", statusCode);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}