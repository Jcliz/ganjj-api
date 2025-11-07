package com.ganjj.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class ProductReviewCreateDTO {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "O ID do produto é obrigatório.")
    private Long productId;

    private Long orderId;

    @NotNull(message = "A avaliação é obrigatória.")
    @Min(value = 1, message = "A avaliação mínima é 1 estrela.")
    @Max(value = 5, message = "A avaliação máxima é 5 estrelas.")
    private Integer rating;

    @Size(max = 1000, message = "O comentário não pode ter mais de 1000 caracteres.")
    private String comment;

    private List<String> imageUrls;
}
