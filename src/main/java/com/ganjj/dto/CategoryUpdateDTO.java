package com.ganjj.dto;

import lombok.Data;

@Data
public class CategoryUpdateDTO {
    
    private String name;
    
    private String description;
    
    private String imageUrl;
    
    private Long parentId;
    
    private Boolean active;
}