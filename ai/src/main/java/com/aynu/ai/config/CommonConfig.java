package com.aynu.ai.config;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.loader.ClassPathDocumentLoader;
import dev.langchain4j.data.document.splitter.DocumentSplitters;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.embedding.onnx.bgesmallzh.BgeSmallZhEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import dev.langchain4j.store.memory.chat.ChatMemoryStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class CommonConfig {
    @Autowired
    private OpenAiChatModel model;

    @Autowired
    private ChatMemoryStore redisChatMemoryStore;

    @Bean
    public ChatMemoryProvider chatMemoryProvider() {
        return memoryId -> MessageWindowChatMemory.builder()
                .id(memoryId)
                .maxMessages(20)
                .chatMemoryStore(redisChatMemoryStore)
                .build();
    }


    @Bean
    public EmbeddingModel embeddingModel() {
        return new BgeSmallZhEmbeddingModel();
    }

    @Bean
    public EmbeddingStore<TextSegment> store(EmbeddingModel embeddingModel) {
        // 1. 加载文件
        List<Document> documents = ClassPathDocumentLoader.loadDocuments("content");

        // 2. 初始化内存数据库
        InMemoryEmbeddingStore<TextSegment> inMemoryEmbeddingStore = new InMemoryEmbeddingStore<>();

        // 3. 构建 Ingestor 并显式指定 embeddingModel
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(DocumentSplitters.recursive(500, 50))
                .embeddingModel(embeddingModel)
                .embeddingStore(inMemoryEmbeddingStore)
                .build();

        // 4. 执行导入
        ingestor.ingest(documents);

        return inMemoryEmbeddingStore;
    }

    // 构建向量数据库检索对象
    @Bean
    public ContentRetriever contentRetriever(EmbeddingStore store, EmbeddingModel embeddingModel) {
        return EmbeddingStoreContentRetriever.builder()
                .embeddingModel(embeddingModel)
                .embeddingStore(store)
                .minScore(0.9)
                .maxResults(6)
                .build();
    }
}
