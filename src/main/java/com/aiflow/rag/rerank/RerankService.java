package com.aiflow.rag.rerank;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
@Service
public class RerankService {

    private final WebClient webClient;
    private final boolean enabled;
    private final Duration timeout;

    public RerankService(
            WebClient.Builder webClientBuilder,
            @Value("${aiflow.rerank.base-url:http://localhost:8787}") String baseUrl,
            @Value("${aiflow.rerank.enabled:true}") boolean enabled,
            @Value("${aiflow.rerank.timeout:30s}") Duration timeout
    ) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        this.enabled = enabled;
        this.timeout = timeout;
    }

    /**
     * 对文档列表进行 rerank，返回按相关性降序排列的结果
     *
     * @param query     查询文本
     * @param documents 文档内容列表
     * @return 按相关性降序排列的 RerankResult 列表，id 对应原始文档的 index
     */
    private static final int MAX_DOCUMENTS = 100;

    public Mono<List<RerankResult>> rerank(String query, List<String> documents) {
        if (!enabled || documents == null || documents.isEmpty()) {
            return Mono.just(fallbackResult(documents == null ? 0 : documents.size()));
        }

        // 防止文档数量过多导致 rerank 服务 OOM
        final List<String> docs = documents.size() > MAX_DOCUMENTS
                ? documents.subList(0, MAX_DOCUMENTS)
                : documents;
        if (documents.size() > MAX_DOCUMENTS) {
            log.warn("Rerank requested with {} documents, capping to {}", documents.size(), MAX_DOCUMENTS);
        }

        List<RerankDocument> rerankDocs = IntStream.range(0, docs.size())
                .mapToObj(i -> new RerankDocument(i, docs.get(i)))
                .toList();

        RerankRequest request = new RerankRequest(query, rerankDocs);

        log.debug("Rerank request: query={}, documents={}", query, docs.size());

        return webClient.post()
                .uri("/api/v1/rerank")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(RerankResponse.class)
                .timeout(timeout)
                .map(RerankResponse::data)
                .doOnNext(results -> log.debug("Rerank results: {}", results))
                .onErrorResume(e -> {
                    log.warn("Rerank failed, falling back to original order", e);
                    return Mono.just(fallbackResult(docs.size()));
                });
    }

    /**
     * Fallback：返回原始顺序（index 0,1,2...），similarity 全为 0
     */
    private List<RerankResult> fallbackResult(int size) {
        List<RerankResult> results = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            results.add(new RerankResult(i, 0.0));
        }
        return results;
    }
}
