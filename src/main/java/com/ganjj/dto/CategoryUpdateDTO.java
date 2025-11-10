package com.ganjj.dto;

import lombok.Data;

@Data
public class CategoryUpdateDTO {
    
    private String name;
    
    private String description;
    
    private Boolean active;
}