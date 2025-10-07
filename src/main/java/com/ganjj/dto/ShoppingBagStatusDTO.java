package com.ganjj.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ShoppingBagStatusDTO {
    @NotNull(message = "O status não pode ser nulo.")
    private String status;
}