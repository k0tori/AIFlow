package com.aiflow.file.consumer;

import com.aiflow.rag.chunk.ChunkSplitter;
import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.rag.entity.KnowledgeDocument;
import com.aiflow.rag.parser.DocumentParser;
import com.aiflow.mapper.KnowledgeChunkMapper;
import com.aiflow.mapper.KnowledgeDocumentMapper;
import com.aiflow.rag.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConsumer {

    private final List<DocumentParser> parsers;
    private final ChunkSplitter chunkSplitter;
    private final EmbeddingService embeddingService;
    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;

    @RabbitListener(queues = "rag.document.parse.queue")
    public void handleParseMessage(Map<String, Object> message) {
        Long documentId = ((Number) message.get("documentId")).longValue();
        String fileType = (String) message.get("fileType");
        String filePath = (String) message.get("filePath");

        log.info("Processing document: {}", documentId);

        try {
            // Update status to PROCESSING
            KnowledgeDocument document = documentMapper.selectById(documentId);
            document.setStatus("PROCESSING");
            documentMapper.updateById(document);

            // Parse document (with proper resource management)
            DocumentParser parser = parsers.stream()
                    .filter(p -> p.supportsType().equals(fileType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No parser for type: " + fileType));

            String content;
            try (FileInputStream fis = new FileInputStream(filePath)) {
                content = parser.parse(fis);
            }

            // Split into chunks
            List<String> chunks = chunkSplitter.split(content);

            // Generate embeddings in parallel, then batch insert
            List<KnowledgeChunk> knowledgeChunks = Flux.fromIterable(chunks)
                    .index()
                    .flatMap(tuple -> {
                        int index = tuple.getT1().intValue();
                        String chunkText = tuple.getT2();
                        return embeddingService.embed(chunkText)
                                .map(embedding -> {
                                    KnowledgeChunk kc = new KnowledgeChunk();
                                    kc.setDocumentId(documentId);
                                    kc.setChunkIndex(index);
                                    kc.setContent(chunkText);
                                    kc.setEmbedding(embedding);
                                    return kc;
                                });
                    }, 4) // concurrency 4 for parallel embedding
                    .collectList()
                    .block();

            // Batch insert chunks
            if (knowledgeChunks != null && !knowledgeChunks.isEmpty()) {
                chunkMapper.insertBatch(knowledgeChunks);
            }

            // Update status to COMPLETED
            document.setStatus("COMPLETED");
            documentMapper.updateById(document);

            log.info("Document processed: {}, chunks: {}", documentId, chunks.size());
        } catch (Exception e) {
            log.error("Failed to process document: {}", documentId, e);

            // Update status to FAILED
            KnowledgeDocument document = documentMapper.selectById(documentId);
            if (document != null) {
                document.setStatus("FAILED");
                documentMapper.updateById(document);
            }
        }
    }
}
