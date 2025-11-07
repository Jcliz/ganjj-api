package com.ganjj.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "tb_shopping_bags")
@Data
@NoArgsConstructor
public class ShoppingBag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShoppingBagStatus status = ShoppingBagStatus.OPEN;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "shoppingBag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ShoppingBagItem> items = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void recalculateTotalAmount() {
        this.totalAmount = items.stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addItem(ShoppingBagItem item) {
        items.add(item);
        item.setShoppingBag(this);
        recalculateTotalAmount();
    }

    public void removeItem(ShoppingBagItem item) {
        items.remove(item);
        item.setShoppingBag(null);
        recalculateTotalAmount();
    }

    public enum ShoppingBagStatus {
        OPEN,
        CHECKOUT,
        COMPLETED,
        ABANDONED
    }
}