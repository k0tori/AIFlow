package com.aiflow.rag.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Slf4j
@Service
public class EmbeddingService {

    private final EmbeddingModel embeddingModel;

    public EmbeddingService(@Qualifier("ollamaEmbeddingModel") EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    public Mono<float[]> embed(String text) {
        return Mono.fromCallable(() -> {
            try {
                return embeddingModel.embed(text);
            } catch (Exception e) {
                log.error("Failed to generate embedding", e);
                throw new RuntimeException("Embedding failed", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    public Mono<List<float[]>> embedBatch(List<String> texts) {
        return Mono.fromCallable(() -> {
            try {
                return embeddingModel.embed(texts);
            } catch (Exception e) {
                log.error("Failed to generate batch embeddings", e);
                throw new RuntimeException("Batch embedding failed", e);
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
