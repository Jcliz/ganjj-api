package com.ganjj.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CategoryCreateDTO {
    
    @NotBlank(message = "O nome da categoria não pode ser vazio.")
    private String name;
    
    private String description;
}