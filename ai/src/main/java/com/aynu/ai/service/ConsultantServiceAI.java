package com.aynu.ai.service;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.spring.AiService;
import dev.langchain4j.service.spring.AiServiceWiringMode;
import reactor.core.publisher.Flux;

/**
 * LangChain4j AI Service 接口
 * 
 * 该接口由 LangChain4j 在运行时通过动态代理实现
 * 返回的 Flux<String> 是原始 token 内容
 */
@AiService(wiringMode = AiServiceWiringMode.EXPLICIT,
        streamingChatModel = "openAiStreamingChatModel",
        chatMemoryProvider = "chatMemoryProvider",
        contentRetriever = "contentRetriever")
public interface ConsultantServiceAI {
    
    /**
     * 流式对话接口
     * 
     * @param memoryId 记忆ID，用于关联上下文
     * @param message 用户发送的消息
     * @return 原始 token 流
     */
    @SystemMessage(fromResource = "system.md")
    Flux<String> chat(@MemoryId String memoryId, @UserMessage String message);
}