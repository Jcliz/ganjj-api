package com.ganjj.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShoppingBagCreateDTO {
    @NotNull(message = "O ID do usuário não pode ser nulo.")
    private Long userId;
}