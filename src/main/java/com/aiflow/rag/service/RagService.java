package com.aiflow.rag.service;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.rag.retrieval.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RagService {

    private final SearchService searchService;

    private final Resource ragPrompt = new ClassPathResource("prompts/rag/rag-prompt.txt");

    public Mono<String> buildRagContext(String question) {
        // Search for relevant chunks with rerank
        return searchService.searchWithRerank(question)
                .map(chunks -> {
                    if (chunks.isEmpty()) {
                        log.info("No relevant chunks found for question: {}", question);
                        return "";
                    }

                    // Build context
                    String context = chunks.stream()
                            .map(KnowledgeChunk::getContent)
                            .collect(Collectors.joining("\n\n"));

                    // Load prompt template
                    String template = readResource(ragPrompt);

                    // Replace placeholders
                    return template
                            .replace("{context}", context)
                            .replace("{question}", question);
                });
    }

    private String readResource(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read RAG prompt", e);
            return "Context:\n{context}\n\nQuestion: {question}";
        }
    }
}
