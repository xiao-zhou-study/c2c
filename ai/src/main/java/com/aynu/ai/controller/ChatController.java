package com.aynu.ai.controller;

import com.aynu.ai.service.ConsultantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/chat")
public class ChatController {
    @Autowired
    private ConsultantService consultantService;

    @RequestMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chat(String memoryId, String message) {
        return consultantService.chat(memoryId, message);
    }
}
