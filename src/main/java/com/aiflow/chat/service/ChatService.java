package com.aiflow.chat.service;

import com.aiflow.agent.service.AgentService;
import com.aiflow.chat.dto.ChatRequest;
import com.aiflow.chat.memory.ChatMemoryService;
import com.aiflow.rag.service.RagService;
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
    private final TokenUsageService tokenUsageService;
    private final RagService ragService;
    private final AgentService agentService;

    private final Resource systemPrompt = new ClassPathResource("prompts/system/system-prompt.txt");

    public Flux<String> streamChat(ChatRequest request) {
        // Load history
        List<String> history = chatMemoryService.getHistory(request.getSessionId());

        // Build RAG context
        String ragContext = ragService.buildRagContext(request.getMessage());

        // Build prompt
        String system = readResource(systemPrompt);
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        String fullPrompt = system + "\n\n" + ragContext + "\n\n" + historyText + "\n\nUser: " + request.getMessage();

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Stream response
        ChatClient chatClient = chatClientBuilder.build();
        return chatClient.prompt()
                .user(fullPrompt)
                .stream()
                .content()
                .doOnComplete(() -> {
                    // TODO: Save assistant message and token usage
                });
    }

    public Flux<String> streamAgentChat(ChatRequest request) {
        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Use agent with tools
        return agentService.streamAgent(request.getMessage())
                .doOnComplete(() -> {
                    // TODO: Save assistant message
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
