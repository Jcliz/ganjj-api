package com.ganjj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class JwtResponseDTO {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String name;
    private String email;
    private String role;
    private List<String> authorities;

    public JwtResponseDTO(String token, String refreshToken, Long id, String name, String email, String role, List<String> authorities) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.authorities = authorities;
    }
}