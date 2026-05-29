package com.aiflow.rag.parser;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class MarkdownParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        // Remove markdown syntax for plain text
        return content
                .replaceAll("#+ ", "")
                .replaceAll("\\*\\*(.*?)\\*\\*", "$1")
                .replaceAll("\\*(.*?)\\*", "$1")
                .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                .replaceAll("```[\\s\\S]*?```", "")
                .replaceAll("`([^`]+)`", "$1");
    }

    @Override
    public String supportsType() {
        return "md";
    }
}
