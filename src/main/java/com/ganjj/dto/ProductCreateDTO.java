package com.ganjj.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ProductCreateDTO {
    
    @NotBlank(message = "O nome do produto não pode ser vazio.")
    private String name;
    
    @NotBlank(message = "A descrição do produto não pode ser vazia.")
    @Size(max = 2000, message = "A descrição não pode ter mais que 2000 caracteres.")
    private String description;
    
    @NotNull(message = "O preço não pode ser nulo.")
    @Min(value = 0, message = "O preço não pode ser negativo.")
    private BigDecimal price;
    
    @NotNull(message = "A quantidade em estoque não pode ser nula.")
    @Min(value = 0, message = "A quantidade em estoque não pode ser negativa.")
    private Integer stockQuantity;
    
    @NotNull(message = "O ID da marca não pode ser nulo.")
    private Long brandId;
    
    private List<String> availableSizes;
    
    private List<String> availableColors;
    
    private Long categoryId;
    
    private String material;
    
    private String careInstructions;
    
    private Boolean featured;
    
    private BigDecimal discountPercent;
}