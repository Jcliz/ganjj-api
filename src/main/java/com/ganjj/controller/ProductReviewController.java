package com.ganjj.controller;

import com.ganjj.dto.ProductReviewCreateDTO;
import com.ganjj.dto.ProductReviewResponseDTO;
import com.ganjj.dto.ProductReviewUpdateDTO;
import com.ganjj.security.UserDetailsImpl;
import com.ganjj.service.ProductReviewService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reviews")
public class ProductReviewController {

    @Autowired
    private ProductReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> createReview(
            @Valid @RequestBody ProductReviewCreateDTO createDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !authenticatedUserId.equals(createDTO.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode criar avaliações para si mesmo.");
        }
        
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
    public ResponseEntity<?> getUserReviews(
            @PathVariable Long userId,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !authenticatedUserId.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode listar suas próprias avaliações.");
        }
        
        return ResponseEntity.ok(reviewService.getUserReviews(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductReviewResponseDTO> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> updateReview(
            @PathVariable Long id,
            @Valid @RequestBody ProductReviewUpdateDTO updateDTO,
            Authentication authentication) {
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        ProductReviewResponseDTO existingReview = reviewService.getReviewById(id);
        
        if (!isAdmin && !authenticatedUserId.equals(existingReview.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode atualizar suas próprias avaliações.");
        }
        
        return ResponseEntity.ok(reviewService.updateReview(id, updateDTO));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteReview(@PathVariable Long id, Authentication authentication) {
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        ProductReviewResponseDTO existingReview = reviewService.getReviewById(id);
        
        if (!isAdmin && !authenticatedUserId.equals(existingReview.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Você só pode deletar suas próprias avaliações.");
        }
        
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

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}
