package com.aiflow.chat.service;

import com.aiflow.chat.entity.ChatUsage;
import com.aiflow.mapper.ChatUsageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenUsageService {

    private final ChatUsageMapper chatUsageMapper;

    public void saveUsage(Long sessionId, String modelName, int promptTokens, int completionTokens) {
        ChatUsage usage = new ChatUsage();
        usage.setSessionId(sessionId);
        usage.setModelName(modelName);
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        chatUsageMapper.insert(usage);
    }
}
