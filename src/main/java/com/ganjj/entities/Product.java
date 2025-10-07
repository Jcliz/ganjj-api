package com.ganjj.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_products")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stockQuantity;

    @ManyToOne
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @ElementCollection
    @CollectionTable(name = "tb_product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_product_sizes", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "size")
    private List<String> availableSizes = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "tb_product_colors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color")
    private List<String> availableColors = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String material;

    private String careInstructions;

    @Column(nullable = false)
    private Boolean active = true;

    private Boolean featured = false;

    private BigDecimal discountPercent = BigDecimal.ZERO;
    
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getCurrentPrice() {
        if (discountPercent == null || discountPercent.compareTo(BigDecimal.ZERO) <= 0) {
            return price;
        }
        
        BigDecimal discountAmount = price.multiply(discountPercent).divide(new BigDecimal(100));
        return price.subtract(discountAmount);
    }
}