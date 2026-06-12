package com.aiflow.chat.service;

import com.aiflow.chat.entity.ChatSession;
import com.aiflow.chat.memory.ChatMemoryService;
import com.aiflow.common.exception.BusinessException;
import com.aiflow.mapper.ChatSessionMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMemoryService chatMemoryService;

    /**
     * Create a new chat session for the given user.
     */
    public ChatSession createSession(Long userId, String title) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setTitle(title != null ? title : "New Chat");
        session.setCreatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    /**
     * List all sessions for a user, ordered by creation time descending.
     */
    public List<ChatSession> listSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .orderByDesc(ChatSession::getCreatedAt)
        );
    }

    /**
     * Get a session by ID, verifying ownership.
     */
    public ChatSession getSession(Long sessionId, Long userId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            throw new BusinessException(404, "Session not found");
        }
        return session;
    }

    /**
     * Update session title.
     */
    public ChatSession updateSession(Long sessionId, Long userId, String title) {
        ChatSession session = getSession(sessionId, userId);
        session.setTitle(title);
        sessionMapper.updateById(session);
        return session;
    }

    /**
     * Delete a session and its Redis memory.
     */
    public void deleteSession(Long sessionId, Long userId) {
        ChatSession session = getSession(sessionId, userId);
        sessionMapper.deleteById(sessionId);
        chatMemoryService.clearHistory(sessionId);
    }
}
