package com.ganjj.repository;

import com.ganjj.entities.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdAndActiveTrue(Long productId);

    List<ProductReview> findByProductId(Long productId);

    List<ProductReview> findByUserId(Long userId);

    List<ProductReview> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<ProductReview> findByUserIdAndProductId(Long userId, Long productId);

    List<ProductReview> findByVerifiedPurchaseTrue();

    List<ProductReview> findByRating(Integer rating);
}
