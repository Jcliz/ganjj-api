package com.ganjj.dto;

import com.ganjj.entities.ShoppingBag;
import lombok.Data;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
public class ShoppingBagSummaryDTO {
    private Long id;
    private Long userId;
    private int itemCount;
    private BigDecimal totalAmount;
    private String status;

    public ShoppingBagSummaryDTO(ShoppingBag shoppingBag) {
        this.id = shoppingBag.getId();
        this.userId = shoppingBag.getUser().getId();
        this.itemCount = shoppingBag.getItems().size();
        this.totalAmount = shoppingBag.getTotalAmount();
        this.status = shoppingBag.getStatus().toString();
    }
    
    public static List<ShoppingBagSummaryDTO> fromEntities(List<ShoppingBag> shoppingBags) {
        List<ShoppingBagSummaryDTO> dtoList = new ArrayList<>();
        for (ShoppingBag bag : shoppingBags) {
            dtoList.add(new ShoppingBagSummaryDTO(bag));
        }
        return dtoList;
    }
}