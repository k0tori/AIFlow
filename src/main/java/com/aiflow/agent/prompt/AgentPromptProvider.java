package com.aiflow.agent.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class AgentPromptProvider {

    private final Resource agentPrompt = new ClassPathResource("prompts/tool/tool-agent-prompt.txt");

    public String getSystemPrompt() {
        try {
            return agentPrompt.getContentAsString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "你是一个智能助手，可以使用工具来帮助用户。";
        }
    }
}
