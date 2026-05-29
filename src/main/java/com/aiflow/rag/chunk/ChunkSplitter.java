package com.aiflow.rag.chunk;

import com.aiflow.rag.strategy.ChunkStrategy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ChunkSplitter {

    public List<String> split(String text, ChunkStrategy strategy) {
        List<String> chunks = new ArrayList<>();
        int length = text.length();
        int start = 0;

        while (start < length) {
            int end = Math.min(start + strategy.getChunkSize(), length);

            // Try to find a sentence boundary
            if (end < length) {
                int lastPeriod = text.lastIndexOf('.', end);
                int lastNewline = text.lastIndexOf('\n', end);
                int boundary = Math.max(lastPeriod, lastNewline);

                if (boundary > start + strategy.getChunkSize() / 2) {
                    end = boundary + 1;
                }
            }

            chunks.add(text.substring(start, end).trim());
            if (end == length) break;
            start = end - strategy.getOverlap();
        }

        return chunks.stream()
                .filter(chunk -> !chunk.isEmpty())
                .toList();
    }

    public List<String> split(String text) {
        return split(text, ChunkStrategy.defaultStrategy());
    }
}
