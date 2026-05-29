package com.aiflow.chat.memory;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ChatMemoryService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String KEY_PREFIX = "chat:memory:";
    private static final int MAX_MESSAGES = 20;
    private static final long EXPIRE_HOURS = 24;

    public List<String> getHistory(Long sessionId) {
        String key = KEY_PREFIX + sessionId;
        List<Object> messages = redisTemplate.opsForList().range(key, 0, -1);
        if (messages == null) {
            return new ArrayList<>();
        }
        return messages.stream()
                .map(Object::toString)
                .toList();
    }

    public void addMessage(Long sessionId, String role, String content) {
        String key = KEY_PREFIX + sessionId;
        String message = role + ":" + content;
        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.opsForList().trim(key, -MAX_MESSAGES, -1);
        redisTemplate.expire(key, EXPIRE_HOURS, TimeUnit.HOURS);
    }

    public void clearHistory(Long sessionId) {
        String key = KEY_PREFIX + sessionId;
        redisTemplate.delete(key);
    }
}
