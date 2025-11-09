package com.ganjj.dto;

import lombok.Data;

@Data
public class BrandUpdateDTO {
    
    private String name;
    
    private String description;
    
    private String website;
    
    private String country;
    
    private Boolean active;
}