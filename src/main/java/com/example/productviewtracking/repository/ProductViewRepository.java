package com.example.productviewtracking.repository;

import com.example.productviewtracking.model.ProductViewEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ProductViewRepository extends ReactiveMongoRepository<ProductViewEvent, String> {

    Mono<Long> countByProductId(String productId);
    
    Flux<ProductViewEvent> findByUserId(String userId);
}