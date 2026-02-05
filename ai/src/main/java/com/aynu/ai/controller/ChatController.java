package com.aynu.ai.controller;

import com.aynu.ai.service.ConsultantService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
    private final ConsultantService consultantService;

    public ChatController(ConsultantService consultantService) {
        this.consultantService = consultantService;
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(String memoryId, String message) {
        return consultantService.chat(memoryId, message)
                .map(content -> ServerSentEvent.<String>builder()
                        .data(content) // 这里会自动处理成 data: content
                        .build());
    }
}
