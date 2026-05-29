package com.aiflow.rag.parser;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Component
public class TxtParser implements DocumentParser {

    @Override
    public String parse(InputStream inputStream) throws Exception {
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    @Override
    public String supportsType() {
        return "txt";
    }
}
