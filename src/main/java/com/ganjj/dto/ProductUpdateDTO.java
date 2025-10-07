package com.ganjj.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductUpdateDTO {
    
    private String name;
    
    private String description;
    
    @Min(value = 0, message = "O preço não pode ser negativo.")
    private BigDecimal price;
    
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa.")
    private Integer stockQuantity;
    
    private String brand;
    
    private List<String> imageUrls;
    
    private List<String> availableSizes;
    
    private List<String> availableColors;
    
    private Long categoryId;
    
    private String material;
    
    private String careInstructions;
    
    private Boolean active;
    
    private Boolean featured;
    
    private BigDecimal discountPercent;
}