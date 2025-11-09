package com.ganjj.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductReviewUpdateDTO {

    @Min(value = 1, message = "A avaliação mínima é 1 estrela.")
    @Max(value = 5, message = "A avaliação máxima é 5 estrelas.")
    private Integer rating;

    @Size(max = 1000, message = "O comentário não pode ter mais de 1000 caracteres.")
    private String comment;

    private Boolean active;
}
