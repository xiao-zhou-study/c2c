package com.aynu.ai.controller;

import com.aynu.ai.service.ConsultantService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * AI 聊天控制器 - 响应式流模式
 */
@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ConsultantService consultantService;

    public ChatController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    /**
     * 流式对话接口（GET，支持 EventSource）
     *
     * @param memoryId 记忆ID，用于关联上下文
     * @param message  用户发送的消息
     * @return SSE 事件流
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE + ";charset=UTF-8")
    public Flux<ServerSentEvent<String>> chat(@RequestParam(value = "memoryId", required = false) String memoryId,
                                              @RequestParam("message") String message) {

        return consultantService.chat(memoryId, message);
    }
}
