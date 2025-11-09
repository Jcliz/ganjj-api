package com.ganjj.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserRoleUpdateDTO {
    
    @NotBlank(message = "A role é obrigatória.")
    @Pattern(regexp = "USER|ADMIN", message = "Role deve ser USER ou ADMIN.")
    private String role;
}
