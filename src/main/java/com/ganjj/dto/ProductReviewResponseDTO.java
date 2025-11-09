package com.ganjj.dto;

import com.ganjj.entities.ProductReview;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ProductReviewResponseDTO {

    private Long id;
    private Long userId;
    private Long productId;
    private Long orderId;
    private Integer rating;
    private String comment;
    private Boolean verifiedPurchase;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductReviewResponseDTO(ProductReview review) {
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.productId = review.getProduct().getId();
        this.orderId = review.getOrder() != null ? review.getOrder().getId() : null;
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.verifiedPurchase = review.getVerifiedPurchase();
        this.active = review.getActive();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}
