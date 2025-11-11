package com.ganjj.dto;

import com.ganjj.entities.Order;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "O ID do usuário é obrigatório.")
    private Long userId;

    @NotNull(message = "O ID da sacola de compras é obrigatório.")
    private Long shoppingBagId;

    @NotNull(message = "O ID do endereço de entrega é obrigatório.")
    private Long addressId;

    @NotNull(message = "O método de pagamento é obrigatório.")
    private Order.PaymentMethod paymentMethod;
}
