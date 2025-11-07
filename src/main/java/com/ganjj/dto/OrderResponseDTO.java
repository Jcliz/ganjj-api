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
    private String userName;
    private List<OrderItemDTO> items;
    private BigDecimal totalAmount;
    private Order.OrderStatus status;
    private Order.PaymentMethod paymentMethod;
    private Order.PaymentStatus paymentStatus;
    
    private String deliveryStreet;
    private String deliveryNumber;
    private String deliveryComplement;
    private String deliveryNeighborhood;
    private String deliveryCity;
    private String deliveryState;
    private String deliveryZipCode;
    private String fullDeliveryAddress;
    
    private LocalDateTime orderDate;
    private LocalDateTime paymentDate;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime cancelledDate;
    
    private String trackingCode;
    private String notes;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponseDTO(Order order) {
        this.id = order.getId();
        this.userId = order.getUser().getId();
        this.userName = order.getUser().getName();
        this.items = order.getItems().stream()
                .map(OrderItemDTO::new)
                .collect(Collectors.toList());
        this.totalAmount = order.getTotalAmount();
        this.status = order.getStatus();
        this.paymentMethod = order.getPaymentMethod();
        this.paymentStatus = order.getPaymentStatus();
        
        this.deliveryStreet = order.getDeliveryStreet();
        this.deliveryNumber = order.getDeliveryNumber();
        this.deliveryComplement = order.getDeliveryComplement();
        this.deliveryNeighborhood = order.getDeliveryNeighborhood();
        this.deliveryCity = order.getDeliveryCity();
        this.deliveryState = order.getDeliveryState();
        this.deliveryZipCode = order.getDeliveryZipCode();
        
        this.fullDeliveryAddress = buildFullAddress(order);
        
        this.orderDate = order.getOrderDate();
        this.paymentDate = order.getPaymentDate();
        this.shippedDate = order.getShippedDate();
        this.deliveredDate = order.getDeliveredDate();
        this.cancelledDate = order.getCancelledDate();
        
        this.trackingCode = order.getTrackingCode();
        this.notes = order.getNotes();
        
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
    }

    private String buildFullAddress(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append(order.getDeliveryStreet()).append(", ").append(order.getDeliveryNumber());
        if (order.getDeliveryComplement() != null && !order.getDeliveryComplement().isEmpty()) {
            sb.append(" - ").append(order.getDeliveryComplement());
        }
        sb.append(", ").append(order.getDeliveryNeighborhood());
        sb.append(", ").append(order.getDeliveryCity()).append(" - ").append(order.getDeliveryState());
        sb.append(", CEP: ").append(order.getDeliveryZipCode());
        return sb.toString();
    }
}
