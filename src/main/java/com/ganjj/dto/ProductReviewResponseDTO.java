package com.ganjj.dto;

import com.ganjj.entities.ProductReview;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class ProductReviewResponseDTO {

    private Long id;
    private Long userId;
    private String userName;
    private Long productId;
    private String productName;
    private Long orderId;
    private Integer rating;
    private String comment;
    private Boolean verifiedPurchase;
    private List<String> imageUrls;
    private Boolean active;
    private Integer helpfulCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ProductReviewResponseDTO(ProductReview review) {
        this.id = review.getId();
        this.userId = review.getUser().getId();
        this.userName = review.getUser().getName();
        this.productId = review.getProduct().getId();
        this.productName = review.getProduct().getName();
        this.orderId = review.getOrder() != null ? review.getOrder().getId() : null;
        this.rating = review.getRating();
        this.comment = review.getComment();
        this.verifiedPurchase = review.getVerifiedPurchase();
        this.imageUrls = review.getImageUrls();
        this.active = review.getActive();
        this.helpfulCount = review.getHelpfulCount();
        this.createdAt = review.getCreatedAt();
        this.updatedAt = review.getUpdatedAt();
    }
}
