package com.aiflow.rag.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final EmbeddingClient embeddingClient;

    public float[] embed(String text) {
        try {
            List<float[]> embeddings = embeddingClient.embed(List.of(text));
            return embeddings.get(0);
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
            throw new RuntimeException("Embedding failed", e);
        }
    }

    public List<float[]> embedBatch(List<String> texts) {
        try {
            return embeddingClient.embed(texts);
        } catch (Exception e) {
            log.error("Failed to generate batch embeddings", e);
            throw new RuntimeException("Batch embedding failed", e);
        }
    }
}
