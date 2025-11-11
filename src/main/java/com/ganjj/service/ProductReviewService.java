package com.ganjj.service;

import com.ganjj.dto.ProductReviewCreateDTO;
import com.ganjj.dto.ProductReviewResponseDTO;
import com.ganjj.dto.ProductReviewUpdateDTO;
import com.ganjj.entities.*;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.OrderRepository;
import com.ganjj.repository.ProductRepository;
import com.ganjj.repository.ProductReviewRepository;
import com.ganjj.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductReviewService {

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Transactional
    public ProductReviewResponseDTO createReview(ProductReviewCreateDTO createDTO) {
        if (createDTO.getRating() < 1 || createDTO.getRating() > 5) {
            throw new ValidationException(ErrorCode.REVIEW_RATING_INVALID);
        }

        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, createDTO.getUserId()));

        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, createDTO.getProductId()));

        Optional<ProductReview> existingReview = reviewRepository.findByUserIdAndProductId(
                createDTO.getUserId(), createDTO.getProductId());

        if (existingReview.isPresent()) {
            throw new ValidationException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(createDTO.getRating());
        review.setComment(createDTO.getComment());

        if (createDTO.getOrderId() != null) {
            Order order = orderRepository.findById(createDTO.getOrderId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, createDTO.getOrderId()));

            if (!order.getUser().getId().equals(user.getId())) {
                throw new ValidationException(ErrorCode.REVIEW_ORDER_NOT_BELONGS_TO_USER);
            }

            boolean productInOrder = order.getItems().stream()
                    .anyMatch(item -> item.getProduct().getId().equals(product.getId()));

            if (productInOrder) {
                review.setVerifiedPurchase(true);
                review.setOrder(order);
            }
        }

        ProductReview savedReview = reviewRepository.save(review);
        return new ProductReviewResponseDTO(savedReview);
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponseDTO> getProductReviews(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, productId);
        }
        return reviewRepository.findByProductIdAndActiveTrue(productId).stream()
                .map(ProductReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponseDTO> getUserReviews(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, userId);
        }
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProductReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductReviewResponseDTO getReviewById(Long id) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND, id));
        return new ProductReviewResponseDTO(review);
    }

    @Transactional
    public ProductReviewResponseDTO updateReview(Long id, ProductReviewUpdateDTO updateDTO) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND, id));

        if (updateDTO.getRating() != null) {
            if (updateDTO.getRating() < 1 || updateDTO.getRating() > 5) {
                throw new ValidationException(ErrorCode.REVIEW_RATING_INVALID);
            }
            review.setRating(updateDTO.getRating());
        }

        if (updateDTO.getComment() != null) {
            review.setComment(updateDTO.getComment());
        }

        if (updateDTO.getActive() != null) {
            review.setActive(updateDTO.getActive());
        }

        ProductReview updatedReview = reviewRepository.save(review);
        return new ProductReviewResponseDTO(updatedReview);
    }

    @Transactional
    public void deleteReview(Long id) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.REVIEW_NOT_FOUND, id));
        
        reviewRepository.delete(review);
    }
}
