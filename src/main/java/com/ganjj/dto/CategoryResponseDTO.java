package com.ganjj.dto;

import com.ganjj.entities.Category;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class CategoryResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private List<CategoryResponseDTO> subcategories;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CategoryResponseDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.imageUrl = category.getImageUrl();
        this.active = category.getActive();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
        
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            this.subcategories = category.getSubcategories().stream()
                    .map(CategoryResponseDTO::new)
                    .collect(Collectors.toList());
        }
    }
}