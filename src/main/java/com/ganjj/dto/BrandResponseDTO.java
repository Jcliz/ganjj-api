package com.ganjj.dto;

import com.ganjj.entities.Brand;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BrandResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private String logoUrl;
    private String website;
    private String country;
    private Boolean active;
    private int productCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public BrandResponseDTO(Brand brand) {
        this.id = brand.getId();
        this.name = brand.getName();
        this.description = brand.getDescription();
        this.logoUrl = brand.getLogoUrl();
        this.website = brand.getWebsite();
        this.country = brand.getCountry();
        this.active = brand.getActive();
        this.productCount = brand.getProducts() != null ? brand.getProducts().size() : 0;
        this.createdAt = brand.getCreatedAt();
        this.updatedAt = brand.getUpdatedAt();
    }
}