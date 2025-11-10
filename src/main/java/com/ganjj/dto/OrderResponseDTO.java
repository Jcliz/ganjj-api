package com.ganjj.dto;

import com.ganjj.entities.Order;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderResponseDTO {

    private Long id;
    private Long userId;
    private Long addressId;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    private LocalDateTime orderDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.addressId = order.getDeliveryAddress() != null ? order.getDeliveryAddress().getId() : null;
        this.items = order.getItems().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentMethod = order.getPaymentMethod();
        this.paymentStatus = order.getPaymentStatus();
        this.orderDate = order.getOrderDate();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
    }
}
