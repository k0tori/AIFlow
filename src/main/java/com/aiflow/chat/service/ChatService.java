package com.aiflow.chat.service;

import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.memory.ChatMemoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient.Builder chatClientBuilder;
    private final ChatMemoryService chatMemoryService;

    private final Resource systemPrompt = new ClassPathResource("prompts/system/system-prompt.txt");

    public Flux<String> streamChat(ChatRequest request) {
        ChatClient chatClient = chatClientBuilder.build();

        // Load history
        List<String> history = chatMemoryService.getHistory(request.getSessionId());

        // Build prompt
        String system = readResource(systemPrompt);
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        String fullPrompt = system + "\n\n" + historyText + "\n\nUser: " + request.getMessage();

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Stream response
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content()
                .doOnComplete(() -> {
                    chatMemoryService.addMessage(request.getSessionId(), "Assistant", "...");
                });
    }

    private String readResource(Resource resource) {
        try {
            return resource.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to read prompt resource", e);
            return "";
        }
    }
}
