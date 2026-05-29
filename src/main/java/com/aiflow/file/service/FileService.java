package com.aiflow.file.service;

import com.aiflow.rag.entity.KnowledgeDocument;
import com.aiflow.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final KnowledgeDocumentMapper documentMapper;
    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "rag.document.exchange";
    private static final String ROUTING_KEY = "rag.document.parse";

    public KnowledgeDocument uploadFile(MultipartFile file) throws IOException {
        // Validate file type
        String fileType = getFileType(file.getOriginalFilename());
        if (!isSupportedType(fileType)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }

        // Save document metadata
        KnowledgeDocument document = new KnowledgeDocument();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setStatus("PENDING");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.insert(document);

        // Send MQ message for async processing
        Map<String, Object> message = Map.of(
                "documentId", document.getId(),
                "fileName", file.getOriginalFilename(),
                "fileType", fileType,
                "filePath", saveFile(file)
        );
        rabbitTemplate.convertAndSend(EXCHANGE, ROUTING_KEY, message);

        log.info("File uploaded: {}, documentId: {}", file.getOriginalFilename(), document.getId());
        return document;
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isSupportedType(String fileType) {
        return fileType.equals("pdf") || fileType.equals("md") ||
                fileType.equals("txt") || fileType.equals("docx");
    }

    private String saveFile(MultipartFile file) throws IOException {
        // TODO: Implement file storage (local or S3)
        return "/tmp/aiflow/" + file.getOriginalFilename();
    }
}
