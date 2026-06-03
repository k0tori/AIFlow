package com.aiflow.rag.rerank;

/**
 * Rerank 返回的单个文档结果
 *
 * @param id         文档在请求中的 index
 * @param similarity 相关性分数（越高越相关）
 */
public record RerankResult(
        int id,
        double similarity
) {}
