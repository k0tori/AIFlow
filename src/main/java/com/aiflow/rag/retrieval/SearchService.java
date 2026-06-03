package com.aiflow.rag.retrieval;

import com.aiflow.rag.entity.KnowledgeChunk;
import com.aiflow.rag.rerank.RerankResult;
import com.aiflow.rag.rerank.RerankService;
import com.aiflow.rag.service.EmbeddingService;
import com.aiflow.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final KnowledgeChunkMapper chunkMapper;
    private final EmbeddingService embeddingService;
    private final RerankService rerankService;

    @Value("${aiflow.rag.retrieve-top-k:20}")
    private int retrieveTopK;

    @Value("${aiflow.rag.rerank-top-n:5}")
    private int rerankTopN;

    private static final int LEGACY_SEARCH_TOP_K = 5;

    /**
     * 纯向量检索（兼容旧接口）
     */
    public Mono<List<KnowledgeChunk>> search(String query) {
        return search(query, LEGACY_SEARCH_TOP_K);
    }

    /**
     * 纯向量检索（指定 topK）
     */
    public Mono<List<KnowledgeChunk>> search(String query, int topK) {
        return embeddingService.embed(query)
                .map(this::convertToPgVector)
                .flatMap(vectorStr -> Mono.fromCallable(() -> {
                    return chunkMapper.similaritySearch(vectorStr, topK);
                }).subscribeOn(Schedulers.boundedElastic()));
    }

    /**
     * 向量检索 + Rerank：Retrieve → Rerank → 返回 Top-N
     */
    public Mono<List<KnowledgeChunk>> searchWithRerank(String query) {
        // Step 1: 扩大召回量进行向量检索
        return embeddingService.embed(query)
                .map(this::convertToPgVector)
                .flatMap(vectorStr -> Mono.fromCallable(() -> {
                    return chunkMapper.similaritySearch(vectorStr, retrieveTopK);
                }).subscribeOn(Schedulers.boundedElastic()))
                .flatMap(candidates -> {
                    if (candidates.isEmpty()) {
                        return Mono.just(List.of());
                    }

                    // Step 2: 提取文档内容，调用 Rerank
                    List<String> contents = candidates.stream()
                            .map(KnowledgeChunk::getContent)
                            .toList();

                    log.info("Rerank: retrieved {} candidates, reranking to top-{}", candidates.size(), rerankTopN);

                    return rerankService.rerank(query, contents)
                            .map(rerankResults -> {
                                // Step 3: 根据 rerank 结果重新排序，取 top-N（去重）
                                List<KnowledgeChunk> reranked = new ArrayList<>();
                                Set<Integer> seen = new HashSet<>();
                                for (RerankResult result : rerankResults) {
                                    if (reranked.size() >= rerankTopN) break;
                                    int idx = result.id();
                                    if (idx >= 0 && idx < candidates.size() && seen.add(idx)) {
                                        KnowledgeChunk chunk = candidates.get(idx);
                                        // 用 rerank 分数覆盖原始相似度
                                        chunk.setSimilarity(result.similarity());
                                        reranked.add(chunk);
                                    }
                                }
                                log.info("Rerank complete: {} chunks selected", reranked.size());
                                return reranked;
                            });
                });
    }

    private String convertToPgVector(float[] embedding) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < embedding.length; i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(String.format("%.6f", embedding[i]));
        }
        sb.append("]");
        return sb.toString();
    }
}
