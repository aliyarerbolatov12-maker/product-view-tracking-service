package com.example.productviewtracking.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "product_view_events")
@Getter
@Setter
@NoArgsConstructor
@ToString
@CompoundIndex(def = "{'productId': 1, 'userId': 1, 'viewedAt': -1}")
public class ProductViewEvent {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Indexed
    private String productId;

    @Indexed
    private String userId;

    @Indexed(expireAfter = "30d")
    private Instant viewedAt;

    @Builder
    public ProductViewEvent(String productId, String userId, Instant viewedAt) {
        this.productId = productId;
        this.userId = userId;
        this.viewedAt = viewedAt;
    }
}

