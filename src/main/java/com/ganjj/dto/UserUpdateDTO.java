package com.ganjj.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDTO {
    
    private String name;
    
    @Email(message = "Formato de e-mail inválido.")
    private String email;
    
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    private String password;
    
    private String phone;
    
    private String role;
}