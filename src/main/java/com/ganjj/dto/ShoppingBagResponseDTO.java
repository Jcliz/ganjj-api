package com.ganjj.dto;

import com.ganjj.entities.ShoppingBag;
import com.ganjj.entities.ShoppingBagItem;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class ShoppingBagResponseDTO {
    private Long id;
    private Long userId;
    private String userName;
    private BigDecimal totalAmount;
    private String status;
    private List<ShoppingBagItemResponseDTO> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ShoppingBagResponseDTO(ShoppingBag shoppingBag) {
        this.id = shoppingBag.getId();
        this.userId = shoppingBag.getUser().getId();
        this.userName = shoppingBag.getUser().getName();
        this.totalAmount = shoppingBag.getTotalAmount();
        this.status = shoppingBag.getStatus().toString();
        this.items = shoppingBag.getItems().stream()
                .map(ShoppingBagItemResponseDTO::new)
                .collect(Collectors.toList());
        this.createdAt = shoppingBag.getCreatedAt();
        this.updatedAt = shoppingBag.getUpdatedAt();
    }

    @Data
    public static class ShoppingBagItemResponseDTO {
        private Long id;
        private String productId;
        private String productName;
        private String size;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal subtotal;

        public ShoppingBagItemResponseDTO(ShoppingBagItem item) {
            this.id = item.getId();
            this.productId = item.getProductId();
            this.productName = item.getProductName();
            this.size = item.getSize();
            this.price = item.getPrice();
            this.quantity = item.getQuantity();
            this.subtotal = item.getSubtotal();
        }
    }
}