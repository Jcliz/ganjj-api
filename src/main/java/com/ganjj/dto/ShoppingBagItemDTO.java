package com.ganjj.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ShoppingBagItemDTO {
    @NotBlank(message = "O ID do produto não pode ser vazio.")
    private String productId;

    @NotBlank(message = "O nome do produto não pode ser vazio.")
    private String productName;

    @NotBlank(message = "A URL da imagem do produto não pode ser vazia.")
    private String productImage;

    @NotBlank(message = "O tamanho não pode ser vazio.")
    private String size;

    @NotNull(message = "O preço não pode ser nulo.")
    @Min(value = 0, message = "O preço não pode ser negativo.")
    private BigDecimal price;

    @NotNull(message = "A quantidade não pode ser nula.")
    @Min(value = 1, message = "A quantidade mínima é 1.")
    private Integer quantity;
}