package com.ganjj.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TokenRefreshResponseDTO {
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
}