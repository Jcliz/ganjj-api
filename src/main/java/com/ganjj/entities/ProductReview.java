package com.ganjj.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_product_reviews")
@Data
@NoArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer rating; 

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private Boolean verifiedPurchase = false;

    @ElementCollection
    @CollectionTable(name = "tb_review_images", joinColumns = @JoinColumn(name = "review_id"))
    @Column(name = "image_url")
    private java.util.List<String> imageUrls = new java.util.ArrayList<>();

    @Column(nullable = false)
    private Boolean active = true;

    private Integer helpfulCount = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
