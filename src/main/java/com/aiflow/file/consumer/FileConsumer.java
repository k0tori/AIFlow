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

            // Parse document
            DocumentParser parser = parsers.stream()
                    .filter(p -> p.supportsType().equals(fileType))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("No parser for type: " + fileType));

            String content = parser.parse(new FileInputStream(filePath));

            // Split into chunks
            List<String> chunks = chunkSplitter.split(content);

            // Generate embeddings and save
            for (int i = 0; i < chunks.size(); i++) {
                String chunk = chunks.get(i);
                float[] embedding = embeddingService.embed(chunk).block();

                KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
                knowledgeChunk.setDocumentId(documentId);
                knowledgeChunk.setChunkIndex(i);
                knowledgeChunk.setContent(chunk);
                knowledgeChunk.setEmbedding(embedding);
                chunkMapper.insert(knowledgeChunk);
            }

            // Update status to COMPLETED
            document.setStatus("COMPLETED");
            documentMapper.updateById(document);

            log.info("Document processed: {}, chunks: {}", documentId, chunks.size());
        } catch (Exception e) {
            log.error("Failed to process document: {}", documentId, e);

            // Update status to FAILED
            KnowledgeDocument document = documentMapper.selectById(documentId);
            document.setStatus("FAILED");
            documentMapper.updateById(document);
        }
    }
}
