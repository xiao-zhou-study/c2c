package com.aynu.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        chatModel = "openAiChatModel",
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever")
public interface ConsultantService {
    @SystemMessage(fromResource = "system.md")
//    @UserMessage("你是我的助手小月月{{msg}}")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}
