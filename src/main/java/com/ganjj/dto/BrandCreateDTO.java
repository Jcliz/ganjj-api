package com.ganjj.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandCreateDTO {
    
    @NotBlank(message = "O nome da marca não pode ser vazio.")
    private String name;
    
    private String description;
    
    private String logoUrl;
    
    private String website;
    
    private String country;
}