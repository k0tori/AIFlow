package com.aiflow.chat.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ChatSession {
    private Long id;
    private Long userId;
    private String title;
    private LocalDateTime createdAt;
}
