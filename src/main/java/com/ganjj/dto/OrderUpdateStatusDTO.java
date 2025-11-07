package com.ganjj.dto;

import com.ganjj.entities.Order;
import lombok.Data;

@Data
public class OrderUpdateStatusDTO {

    private Order.OrderStatus orderStatus;
    private Order.PaymentStatus paymentStatus;
    private String trackingCode;
}
