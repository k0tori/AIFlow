package com.aiflow.rag.strategy;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChunkStrategy {
    private int chunkSize;
    private int overlap;

    public static ChunkStrategy defaultStrategy() {
        return ChunkStrategy.builder()
                .chunkSize(500)
                .overlap(100)
                .build();
    }
}
