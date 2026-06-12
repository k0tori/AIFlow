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
import java.util.StringJoiner;
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

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Build prompt
        String system = readResource(systemPrompt);
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        // Try RAG context, fallback to simple chat if it fails
        return ragService.buildRagContext(request.getMessage())
                .onErrorResume(e -> {
                    log.warn("RAG context building failed, falling back to simple chat: {}", e.getMessage());
                    return reactor.core.publisher.Mono.just("");
                })
                .flatMapMany(ragContext -> {
                    String fullPrompt = system + "\n\n" + ragContext + "\n\n" + historyText + "\n\nUser: " + request.getMessage();

                    // Collect streamed content for persistence
                    StringJoiner responseCollector = new StringJoiner("");

                    // Stream response
                    ChatClient chatClient = chatClientBuilder.build();
                    return chatClient.prompt()
                            .user(fullPrompt)
                            .stream()
                            .content()
                            .doOnNext(token -> responseCollector.add(token))
                            .doOnComplete(() -> {
                                try {
                                    String fullResponse = responseCollector.toString();
                                    // Save assistant message to Redis memory
                                    chatMemoryService.addMessage(request.getSessionId(), "Assistant", fullResponse);
                                    // Record token usage (estimate from character count)
                                    int estimatedTokens = fullResponse.length() / 2;
                                    tokenUsageService.saveUsage(request.getSessionId(), "deepseek-chat", 0, estimatedTokens);
                                    log.debug("Saved assistant message for session: {}, length: {}", request.getSessionId(), fullResponse.length());
                                } catch (Exception e) {
                                    log.error("Failed to save assistant message for session: {}", request.getSessionId(), e);
                                }
                            });
                });
    }

    public Flux<String> streamAgentChat(ChatRequest request) {
        // Load history for agent context
        List<String> history = chatMemoryService.getHistory(request.getSessionId());

        // Save user message
        chatMemoryService.addMessage(request.getSessionId(), "User", request.getMessage());

        // Build context from history
        String historyText = history.stream()
                .map(h -> {
                    String[] parts = h.split(":", 2);
                    return parts.length == 2 ? parts[0] + ": " + parts[1] : h;
                })
                .collect(Collectors.joining("\n"));

        String agentInput = historyText.isEmpty()
                ? request.getMessage()
                : historyText + "\n\nUser: " + request.getMessage();

        // Collect streamed content for persistence
        StringJoiner responseCollector = new StringJoiner("");

        // Use agent with tools
        return agentService.streamAgent(agentInput)
                .doOnNext(token -> responseCollector.add(token))
                .doOnComplete(() -> {
                    try {
                        String fullResponse = responseCollector.toString();
                        chatMemoryService.addMessage(request.getSessionId(), "Assistant", fullResponse);
                        log.debug("Saved agent response for session: {}, length: {}", request.getSessionId(), fullResponse.length());
                    } catch (Exception e) {
                        log.error("Failed to save agent response for session: {}", request.getSessionId(), e);
                    }
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
