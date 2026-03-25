package com.aynu.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Map;

/**
 * AI 聊天控制器 - 响应式流模式
 * <p>
 * 解决 SSE 换行符丢失问题：
 * LangChain4j 返回的 Flux<String> 是原始 token 内容（如 "步骤一\n步骤二"）
 * SSE 协议规定 \n 是字段边界，所以需要将内容包装成 JSON 格式再发送
 * 这样 \n 就变成 JSON 字符串里的转义字符，不会被 SSE 解析器截断
 */
@Service
public class ConsultantService {

    private final ConsultantServiceAI delegate;
    private final ObjectMapper objectMapper;

    public ConsultantService(ConsultantServiceAI delegate) {
        this.delegate = delegate;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 流式对话接口 - 自动包装 SSE 格式
     *
     * @param memoryId 记忆ID，用于关联上下文
     * @param message  用户发送的消息
     * @return SSE 格式的响应式文本流
     */
    public Flux<String> chat(String memoryId, String message) {
        return delegate.chat(memoryId, message)
                .map(token -> {
                    // 将原始 token 内容包装成 JSON 格式
                    // 这样 \n 在 JSON 字符串里是转义字符，不会被 SSE 解析器截断
                    try {
                        return "data: " + objectMapper.writeValueAsString(Map.of("content", token)) + "\n\n";
                    } catch (Exception e) {
                        return "data: {\"content\":\"" + escapeJson(token) + "\"}\n\n";
                    }
                })
                .log("ai.stream.logger");
    }

    /**
     * JSON 转义处理
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}