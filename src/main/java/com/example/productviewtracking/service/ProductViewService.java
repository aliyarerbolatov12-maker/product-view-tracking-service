package com.example.productviewtracking.service;

import com.example.productviewtracking.dto.TopProductResponse;
import com.example.productviewtracking.dto.ViewCountResponse;
import com.example.productviewtracking.mapper.ProductViewMapper;
import com.example.productviewtracking.model.ProductViewEvent;
import com.example.productviewtracking.repository.ProductViewRedisRepository;
import com.example.productviewtracking.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductViewService {

    private static final long MAX_TOP_LIMIT = 100;
    private static final int MAX_HISTORY_LIMIT = 200;

    private final ProductViewRepository productViewRepository;
    private final ProductViewRedisRepository productViewRedisRepository;
    private final ProductViewMapper mapper;

    public Mono<Void> recordView(String productId, String userId) {
        ProductViewEvent event = new ProductViewEvent(productId, userId, Instant.now());

        return productViewRepository.save(event)
                .then(Mono.defer(() -> productViewRedisRepository.recordView(productId)
                        .onErrorResume(e -> {
                            log.warn("Redis failed, skipping cache update", e);
                            return Mono.empty();
                        })));
    }

    public Mono<ViewCountResponse> getViewCount(String productId) {
        return productViewRedisRepository.getViewCount(productId)
                .map(Long::parseLong)
                .onErrorResume(e -> {
                    log.warn("Redis read failed, falling back to MongoDB", e);
                    return Mono.empty();
                })
                .switchIfEmpty(Mono.defer(() -> loadCountFromMongoAndCache(productId)))
                .map(count -> mapper.toViewCountResponse(productId, count));
    }

    public Flux<TopProductResponse> getTopProducts(long limit) {
        long safeLimit = Math.min(Math.max(limit, 1), MAX_TOP_LIMIT);

        return productViewRedisRepository.getTop(safeLimit)
                .map(mapper::toTopProductResponse)
                .onErrorResume(e -> {
                    log.warn("Failed to get top products from Redis", e);
                    return Flux.empty();
                });
    }

    public Flux<ProductViewEvent> getUserHistory(String userId, int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), MAX_HISTORY_LIMIT);
        Pageable page = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "viewedAt"));

        return productViewRepository.findByUserId(userId, page);
    }

    private Mono<Long> loadCountFromMongoAndCache(String productId) {
        return productViewRepository.countByProductId(productId)
                .flatMap(count -> productViewRedisRepository.cacheViewCount(productId, count)
                        .onErrorResume(e -> {
                            log.warn("Failed to cache view count in Redis", e);
                            return Mono.empty();
                        })
                        .thenReturn(count));
    }
}