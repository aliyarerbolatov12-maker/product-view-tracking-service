package com.example.productviewtracking.controller;

import com.example.productviewtracking.dto.ProductViewRequest;
import com.example.productviewtracking.dto.TopProductResponse;
import com.example.productviewtracking.dto.ViewCountResponse;
import com.example.productviewtracking.model.ProductViewEvent;
import com.example.productviewtracking.service.ProductViewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/views")
@RequiredArgsConstructor
public class ProductViewController {

    private final ProductViewService service;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<Void> recordView(@Valid @RequestBody ProductViewRequest request) {
        return service.recordView(request.productId(), request.userId());
    }

    @GetMapping("/{productId}/count")
    public Mono<ViewCountResponse> getCount(@PathVariable String productId) {
        return service.getViewCount(productId);
    }

    @GetMapping("/top")
    public Flux<TopProductResponse> top(@RequestParam(defaultValue = "5") int limit) {
        return service.getTopProducts(limit);
    }

    @GetMapping("/user/{userId}")
    public Flux<ProductViewEvent> userHistory(
            @PathVariable String userId,
            @RequestParam(defaultValue = "50") int limit) {
        return service.getUserHistory(userId, limit);
    }
}