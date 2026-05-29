package com.aiflow.agent.service;

import com.aiflow.agent.prompt.AgentPromptProvider;
import com.aiflow.agent.tool.CourseSearchTool;
import com.aiflow.agent.tool.WeatherTool;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class AgentService {

    private final ChatClient.Builder chatClientBuilder;
    private final WeatherTool weatherTool;
    private final CourseSearchTool courseSearchTool;
    private final AgentPromptProvider promptProvider;

    public Flux<String> streamAgent(String message) {
        ChatClient chatClient = chatClientBuilder
                .defaultTools(weatherTool, courseSearchTool)
                .defaultSystem(promptProvider.getSystemPrompt())
                .build();

        return chatClient.prompt()
                .user(message)
                .stream()
                .content();
    }
}
