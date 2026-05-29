package com.aiflow.rag.chunk;

import com.aiflow.rag.strategy.ChunkStrategy;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ChunkSplitterTest {

    private final ChunkSplitter chunkSplitter = new ChunkSplitter();

    @Test
    void shouldSplitText() {
        String text = "This is a test document. It contains multiple sentences. " +
                "Each sentence has different length. The splitter should handle them properly.";

        List<String> chunks = chunkSplitter.split(text, ChunkStrategy.builder()
                .chunkSize(50)
                .overlap(10)
                .build());

        assertFalse(chunks.isEmpty());
        assertTrue(chunks.size() > 1);
    }

    @Test
    void shouldUseDefaultStrategy() {
        String text = "A".repeat(1000);
        List<String> chunks = chunkSplitter.split(text);

        assertFalse(chunks.isEmpty());
    }
}
