package com.example.productviewtracking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class ProductViewRedisRepository {

    private static final String VIEW_KEY = "product:views:";
    private static final String TOP_KEY = "product:views:top";

    private final ReactiveStringRedisTemplate redis;

    @Value("${redis.product-view.ttl}")
    private Duration viewTtl;

    public Mono<Long> incrementView(String productId) {
        return redis.opsForValue().increment(VIEW_KEY + productId);
    }

    public Mono<Double> incrementTop(String productId) {
        return redis.opsForZSet().incrementScore(TOP_KEY, productId, 1);
    }

    public Mono<Boolean> expireView(String productId) {
        return redis.expire(VIEW_KEY + productId, viewTtl);
    }

    public Mono<String> getViewCount(String productId) {
        return redis.opsForValue().get(VIEW_KEY + productId);
    }

    public Mono<Void> cacheViewCount(String productId, Long count) {
        return redis.opsForValue()
                .set(VIEW_KEY + productId, String.valueOf(count), viewTtl)
                .then();
    }

    public Flux<ZSetOperations.TypedTuple<String>> getTop(long limit) {
        return redis.opsForZSet()
                .reverseRangeWithScores(TOP_KEY, Range.closed(0L, limit - 1));
    }
    
    public Mono<Void> recordView(String productId) {
        return incrementView(productId)
                .then(incrementTop(productId))
                .then(expireView(productId))
                .then();
    }
}
