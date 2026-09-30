package com.example.productviewtracking.dto;

import jakarta.validation.constraints.NotBlank;

public record ProductViewRequest(
        @NotBlank String productId,
        String userId
) {
}