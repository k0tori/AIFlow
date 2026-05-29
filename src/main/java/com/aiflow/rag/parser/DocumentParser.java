package com.aiflow.rag.parser;

import java.io.InputStream;

public interface DocumentParser {
    String parse(InputStream inputStream) throws Exception;
    String supportsType();
}
