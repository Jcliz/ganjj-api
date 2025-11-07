package com.ganjj.controller;

import com.ganjj.dto.ProductReviewCreateDTO;
import com.ganjj.dto.ProductReviewResponseDTO;
import com.ganjj.dto.ProductReviewUpdateDTO;
import com.ganjj.service.ProductReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProductReviewResponseDTO> createReview(@Valid @RequestBody ProductReviewCreateDTO createDTO) {
        ProductReviewResponseDTO createdReview = reviewService.createReview(createDTO);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdReview.getId()).toUri();

        return ResponseEntity.created(uri).body(createdReview);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ProductReviewResponseDTO>> getProductReviews(@PathVariable Long productId) {
        return ResponseEntity.ok(reviewService.getProductReviews(productId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<ProductReviewResponseDTO>> getUserReviews(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReviewResponseDTO> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @GetMapping("/product/{productId}/average")
    public ResponseEntity<Map<String, Object>> getProductRatingStats(@PathVariable Long productId) {
        Double average = reviewService.getProductAverageRating(productId);
        Long count = reviewService.getProductReviewCount(productId);

        return ResponseEntity.ok(Map.of(
                "averageRating", average,
                "reviewCount", count
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<ProductReviewResponseDTO> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ProductReviewUpdateDTO updateDTO) {
        return ResponseEntity.ok(reviewService.updateReview(id, updateDTO));
    }

    @PostMapping("/{id}/helpful")
    public ResponseEntity<ProductReviewResponseDTO> markAsHelpful(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.markAsHelpful(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
