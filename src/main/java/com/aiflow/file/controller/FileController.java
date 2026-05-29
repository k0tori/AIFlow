package com.aiflow.file.controller;

import com.aiflow.common.result.Result;
import com.aiflow.file.service.FileService;
import com.aiflow.rag.entity.KnowledgeDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @PostMapping("/upload")
    public Result<KnowledgeDocument> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            return Result.success(fileService.uploadFile(file));
        } catch (Exception e) {
            return Result.error("Upload failed: " + e.getMessage());
        }
    }
}
