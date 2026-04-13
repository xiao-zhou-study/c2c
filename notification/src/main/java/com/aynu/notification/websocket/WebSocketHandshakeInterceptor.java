package com.aynu.notification.websocket;

import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * WebSocket 握手拦截器
 * 从 URL 参数中获取 userId 并传递到 WebSocketHandler
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, @NonNull ServerHttpResponse response,
                                   @NonNull WebSocketHandler wsHandler, @NonNull Map<String, Object> attributes) {
        // 从查询参数中获取 userId
        String query = request.getURI().getQuery();
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2 && "userId".equals(kv[0])) {
                    try {
                        Long userId = Long.parseLong(kv[1]);
                        attributes.put("userId", userId);
                        log.info("WebSocket handshake: userId={}", userId);
                        return true;
                    } catch (NumberFormatException e) {
                        log.warn("Invalid userId in WebSocket handshake: {}", kv[1]);
                    }
                }
            }
        }
        log.warn("WebSocket handshake rejected: missing userId parameter");
        return false;
    }

    @Override
    public void afterHandshake(@NonNull ServerHttpRequest request, @NonNull ServerHttpResponse response,
                               @NonNull WebSocketHandler wsHandler, Exception exception) {
    }
}
