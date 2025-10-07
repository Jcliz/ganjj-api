package com.ganjj.dto;

import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal currentPrice;
    private Integer stockQuantity;
    private String brand;
    private List<String> imageUrls;
    private List<String> availableSizes;
    private List<String> availableColors;
    private CategoryDTO category;
    private String material;
    private String careInstructions;
    private Boolean active;
    private Boolean featured;
    private BigDecimal discountPercent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.currentPrice = product.getCurrentPrice();
        this.stockQuantity = product.getStockQuantity();
        this.brand = product.getBrand();
        this.imageUrls = product.getImageUrls();
        this.availableSizes = product.getAvailableSizes();
        this.availableColors = product.getAvailableColors();
        
        if (product.getCategory() != null) {
            this.category = new CategoryDTO(product.getCategory());
        }
        
        this.material = product.getMaterial();
        this.careInstructions = product.getCareInstructions();
        this.active = product.getActive();
        this.featured = product.getFeatured();
        this.discountPercent = product.getDiscountPercent();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
    }
    
    @Data
    public static class CategoryDTO {
        private Long id;
        private String name;
        
        public CategoryDTO(Category category) {
            this.id = category.getId();
            this.name = category.getName();
        }
    }
}