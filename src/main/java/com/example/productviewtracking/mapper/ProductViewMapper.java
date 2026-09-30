package com.example.productviewtracking.mapper;

import com.example.productviewtracking.dto.TopProductResponse;
import com.example.productviewtracking.dto.ViewCountResponse;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@Component
public class ProductViewMapper {

    public TopProductResponse toTopProductResponse(
            TypedTuple<String> tuple
    ) {
        return new TopProductResponse(
                tuple.getValue(),
                tuple.getScore().longValue()
        );
    }

    public ViewCountResponse toViewCountResponse(String productId, long count) {
        return new ViewCountResponse(productId, count);
    }
}