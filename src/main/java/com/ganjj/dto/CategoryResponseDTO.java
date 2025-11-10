package com.ganjj.dto;

import com.ganjj.entities.Category;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryResponseDTO {
    
    private Long id;
    private String name;
    private String description;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CategoryResponseDTO(Category category) {
        this.id = category.getId();
        this.name = category.getName();
        this.description = category.getDescription();
        this.active = category.getActive();
        this.createdAt = category.getCreatedAt();
        this.updatedAt = category.getUpdatedAt();
    }
}