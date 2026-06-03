package com.aiflow.rag.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class KnowledgeChunk {
    private Long id;
    private Long documentId;
    private Integer chunkIndex;
    private String content;
    private float[] embedding;
    private Double similarity;
    private LocalDateTime createdAt;
}
