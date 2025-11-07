package com.ganjj.service;

import com.ganjj.dto.ProductReviewCreateDTO;
import com.ganjj.dto.ProductReviewResponseDTO;
import com.ganjj.dto.ProductReviewUpdateDTO;
import com.ganjj.entities.*;
import com.ganjj.repository.OrderRepository;
import com.ganjj.repository.ProductRepository;
import com.ganjj.repository.ProductReviewRepository;
import com.ganjj.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
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
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + createDTO.getUserId()));

        Product product = productRepository.findById(createDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + createDTO.getProductId()));

        // Verificar se o usuário já avaliou este produto
        Optional<ProductReview> existingReview = reviewRepository.findByUserIdAndProductId(
                createDTO.getUserId(), createDTO.getProductId());

        if (existingReview.isPresent()) {
            throw new IllegalArgumentException("Você já avaliou este produto. Use a edição para atualizar sua avaliação.");
        }

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(createDTO.getRating());
        review.setComment(createDTO.getComment());
        review.setImageUrls(createDTO.getImageUrls() != null ? createDTO.getImageUrls() : new java.util.ArrayList<>());

        // Verificar se é compra verificada
        if (createDTO.getOrderId() != null) {
            Order order = orderRepository.findById(createDTO.getOrderId())
                    .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + createDTO.getOrderId()));

            if (!order.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("O pedido não pertence ao usuário informado.");
            }

            // Verificar se o produto está no pedido
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
            throw new EntityNotFoundException("Produto não encontrado com o ID: " + productId);
        }
        return reviewRepository.findByProductIdAndActiveTrue(productId).stream()
                .map(ProductReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ProductReviewResponseDTO> getUserReviews(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + userId);
        }
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(ProductReviewResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductReviewResponseDTO getReviewById(Long id) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com o ID: " + id));
        return new ProductReviewResponseDTO(review);
    }

    @Transactional
    public ProductReviewResponseDTO updateReview(Long id, ProductReviewUpdateDTO updateDTO) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com o ID: " + id));

        if (updateDTO.getRating() != null) {
            review.setRating(updateDTO.getRating());
        }

        if (updateDTO.getComment() != null) {
            review.setComment(updateDTO.getComment());
        }

        if (updateDTO.getImageUrls() != null) {
            review.setImageUrls(updateDTO.getImageUrls());
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
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com o ID: " + id));
        
        reviewRepository.delete(review);
    }

    @Transactional
    public ProductReviewResponseDTO markAsHelpful(Long id) {
        ProductReview review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Avaliação não encontrada com o ID: " + id));

        review.setHelpfulCount(review.getHelpfulCount() + 1);
        ProductReview updatedReview = reviewRepository.save(review);
        return new ProductReviewResponseDTO(updatedReview);
    }

    @Transactional(readOnly = true)
    public Double getProductAverageRating(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Produto não encontrado com o ID: " + productId);
        }
        Double average = reviewRepository.getAverageRatingByProductId(productId);
        return average != null ? average : 0.0;
    }

    @Transactional(readOnly = true)
    public Long getProductReviewCount(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new EntityNotFoundException("Produto não encontrado com o ID: " + productId);
        }
        return reviewRepository.countByProductId(productId);
    }
}
