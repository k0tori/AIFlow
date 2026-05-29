package com.aiflow.rag.retrieval;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.mapper.KnowledgeChunkMapper;
import com.aiflow.rag.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingService embeddingService;

    private static final int DEFAULT_TOP_K = 5;

    public List<KnowledgeChunk> search(String query) {
        return search(query, DEFAULT_TOP_K);
    }

    public List<KnowledgeChunk> search(String query, int topK) {
        // Generate query embedding
        float[] queryEmbedding = embeddingService.embed(query);

        // Convert to pgvector format
        String vectorStr = convertToPgVector(queryEmbedding);

        // Perform similarity search
        return chunkMapper.similaritySearch(vectorStr, topK);
    }

    private String convertToPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(embedding[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
