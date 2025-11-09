package com.ganjj.dto;

import com.ganjj.entities.Brand;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private BrandDTO brand;
    private CategoryDTO category;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.name = product.getName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        if (product.getBrand() != null) {
            this.brand = new BrandDTO(product.getBrand());
        }
        if (product.getCategory() != null) {
            this.category = new CategoryDTO(product.getCategory());
        }
        this.active = product.getActive();
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
    
    @Data
    public static class BrandDTO {
        private Long id;
        private String name;
        
        public BrandDTO(Brand brand) {
            this.id = brand.getId();
            this.name = brand.getName();
        }
    }
}
