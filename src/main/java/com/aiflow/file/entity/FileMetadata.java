package com.aiflow.file.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileMetadata {
    private Long id;
    private String fileName;
    private String fileType;
    private Long fileSize;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
