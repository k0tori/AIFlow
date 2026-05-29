package com.aiflow.chat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatUsage {
    private Long id;
    private Long sessionId;
    private String modelName;
    private Integer promptTokens;
    private Integer completionTokens;
    private Integer totalTokens;
    private LocalDateTime createdAt;
}
