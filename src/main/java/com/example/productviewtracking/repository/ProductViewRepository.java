package com.example.productviewtracking.repository;

import com.example.productviewtracking.model.ProductViewEvent;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface ProductViewRepository extends ReactiveMongoRepository<ProductViewEvent, String> {

    Flux<ProductViewEvent> findByProductId(String productId);

    Flux<ProductViewEvent> findByUserId(String userId);
}