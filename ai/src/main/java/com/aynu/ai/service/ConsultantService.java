package com.aynu.ai.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.Map;

@Service
public class ConsultantService {

    private final ConsultantServiceAI delegate;
    private final ObjectMapper objectMapper;

    public ConsultantService(ConsultantServiceAI delegate, ObjectMapper objectMapper) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;
    }

    /**
     * 流式对话，每个 token 包装为标准 SSE ServerSentEvent 对象
     */
    public Flux<ServerSentEvent<String>> chat(String memoryId, String message) {
        return delegate.chat(memoryId, message)
                .map(token -> {
                    String json = toJson(Map.of("content", token));
                    return ServerSentEvent.<String>builder()
                            .data(json)
                            .build();
                })
                .timeout(Duration.ofMinutes(3))
                .doOnError(e -> {
                    // 流超时或异常时的兜底日志
                });
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "{\"content\":\"\"}";
        }
    }
}
