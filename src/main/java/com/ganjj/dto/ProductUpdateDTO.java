package com.ganjj.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateDTO {
    
    private String name;
    
    private String description;
    
    @Min(value = 0, message = "O preco nao pode ser negativo.")
    private BigDecimal price;
    
    @Min(value = 0, message = "A quantidade em estoque nao pode ser negativa.")
    private Integer stockQuantity;
    
    private Long brandId;
    
    private Long categoryId;
    
    private Boolean active;
}