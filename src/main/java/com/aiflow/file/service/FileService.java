package com.aiflow.file.service;

import com.aiflow.common.config.RabbitMqConfig;
import com.aiflow.rag.entity.KnowledgeDocument;
import com.aiflow.mapper.KnowledgeDocumentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final KnowledgeDocumentMapper documentMapper;
    private final RabbitTemplate rabbitTemplate;

    @Value("${aiflow.file.upload-dir:${java.io.tmpdir}/aiflow/upload}")
    private String uploadDir;

    public KnowledgeDocument uploadFile(MultipartFile file) throws IOException {
        String fileType = getFileType(file.getOriginalFilename());
        if (!isSupportedType(fileType)) {
            throw new IllegalArgumentException("Unsupported file type: " + fileType);
        }

        String filePath = saveFile(file);

        KnowledgeDocument document = new KnowledgeDocument();
        document.setFileName(file.getOriginalFilename());
        document.setFileType(fileType);
        document.setFileSize(file.getSize());
        document.setStatus("PENDING");
        document.setCreatedAt(LocalDateTime.now());
        document.setUpdatedAt(LocalDateTime.now());
        documentMapper.insert(document);

        Map<String, Object> message = Map.of(
                "documentId", document.getId(),
                "fileName", file.getOriginalFilename(),
                "fileType", fileType,
                "filePath", filePath
        );
        rabbitTemplate.convertAndSend(RabbitMqConfig.EXCHANGE, RabbitMqConfig.ROUTING_KEY, message);

        log.info("File uploaded: {}, documentId: {}, path: {}", file.getOriginalFilename(), document.getId(), filePath);
        return document;
    }

    private String getFileType(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isSupportedType(String fileType) {
        return "pdf".equals(fileType) || "md".equals(fileType) ||
                "txt".equals(fileType) || "docx".equals(fileType);
    }

    private String saveFile(MultipartFile file) throws IOException {
        Path dir = Paths.get(uploadDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : "";
        String uniqueName = UUID.randomUUID() + ext;

        Path target = dir.resolve(uniqueName);
        file.transferTo(target.toFile());

        log.info("Saved file: {} -> {}", originalName, target.toAbsolutePath());
        return target.toAbsolutePath().toString();
    }
}
