package com.example.productviewtracking.service;

import com.example.productviewtracking.dto.TopProductResponse;
import com.example.productviewtracking.dto.ViewCountResponse;
import com.example.productviewtracking.mapper.ProductViewMapper;
import com.example.productviewtracking.model.ProductViewEvent;
import com.example.productviewtracking.repository.ProductViewRedisRepository;
import com.example.productviewtracking.repository.ProductViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductViewService {

    private final ProductViewRepository productViewRepository;
    private final ProductViewRedisRepository productViewRedisRepository;
    private final ProductViewMapper mapper;

    public Mono<Void> recordView(String productId, String userId) {
        ProductViewEvent event = new ProductViewEvent(productId, userId, Instant.now());

        Mono<Void> saveToMongo = productViewRepository.save(event).then();

        Mono<Void> updateRedis = productViewRedisRepository.recordView(productId)
                .onErrorResume(e -> {
                    log.warn("Redis failed, skipping cache update", e);
                    return Mono.empty();
                });

        return Mono.when(saveToMongo, updateRedis);
    }

    public Mono<ViewCountResponse> getViewCount(String productId) {
        return productViewRedisRepository.getViewCount(productId)
                .map(Long::parseLong)
                .map(count -> mapper.toViewCountResponse(productId, count))
                .switchIfEmpty(
                        productViewRepository.findByProductId(productId)
                                .count()
                                .flatMap(count -> productViewRedisRepository.cacheViewCount(productId, count)
                                        .thenReturn(mapper.toViewCountResponse(productId, count)))
                );
    }

    public Flux<TopProductResponse> getTopProducts(long limit) {
        return productViewRedisRepository.getTop(limit)
                .map(mapper::toTopProductResponse)
                .onErrorResume(e -> {
                    log.warn("Failed to get top products from Redis", e);
                    return Flux.empty();
                });
    }

    public Flux<ProductViewEvent> getUserHistory(String userId) {
        return productViewRepository.findByUserId(userId);
    }
}
