package com.aiflow.rag.rerank;

import java.util.List;

/**
 * Rerank 服务请求体
 *
 * @param query     查询文本
 * @param documents 待排序的文档列表
 */
public record RerankRequest(
        String query,
        List<RerankDocument> documents
) {}
