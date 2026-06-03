package com.aiflow.rag.rerank;

import java.util.List;

/**
 * Rerank 服务响应体
 *
 * @param data 按相关性降序排列的结果列表
 */
public record RerankResponse(
        List<RerankResult> data
) {}
