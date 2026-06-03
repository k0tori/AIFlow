package com.aiflow.rag.rerank;

/**
 * 发送给 rerank 服务的文档
 *
 * @param id   文档标识（使用原始列表的 index）
 * @param text 文档文本内容
 */
public record RerankDocument(
        int id,
        String text
) {}
