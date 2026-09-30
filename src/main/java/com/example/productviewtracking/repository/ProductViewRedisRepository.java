package com.example.productviewtracking.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProductViewRedisRepository {

    private static final String VIEW_KEY = "product:views:";
    private static final String TOP_KEY = "product:top";
    
    private static final RedisScript<Long> INCREMENT_IF_CACHED = new DefaultRedisScript<>("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                local value = redis.call('INCR', KEYS[1])
                redis.call('EXPIRE', KEYS[1], ARGV[1])
                return value
            end
            return -1
            """, Long.class);

    private final ReactiveStringRedisTemplate redis;

    @Value("${redis.product-view.ttl}")
    private Duration viewTtl;

    public Mono<Long> incrementViewIfCached(String productId) {
        return redis.execute(
                        INCREMENT_IF_CACHED,
                        List.of(VIEW_KEY + productId),
                        List.of(String.valueOf(viewTtl.toSeconds())))
                .next();
    }

    public Mono<Double> incrementTop(String productId) {
        return redis.opsForZSet().incrementScore(TOP_KEY, productId, 1);
    }

    public Mono<String> getViewCount(String productId) {
        return redis.opsForValue().get(VIEW_KEY + productId);
    }

    public Mono<Void> cacheViewCount(String productId, Long count) {
        return redis.opsForValue()
                .set(VIEW_KEY + productId, String.valueOf(count), viewTtl)
                .then();
    }

    public Flux<TypedTuple<String>> getTop(long limit) {
        return redis.opsForZSet()
                .reverseRangeWithScores(TOP_KEY, Range.closed(0L, limit - 1));
    }

    public Mono<Void> recordView(String productId) {
        return incrementViewIfCached(productId)
                .then(incrementTop(productId))
                .then();
    }
}