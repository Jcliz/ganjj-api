package com.ganjj.repository;

import com.ganjj.entities.ShoppingBag;
import com.ganjj.entities.ShoppingBagItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingBagItemRepository extends JpaRepository<ShoppingBagItem, Long> {
    List<ShoppingBagItem> findByShoppingBag(ShoppingBag shoppingBag);
    
    Optional<ShoppingBagItem> findByShoppingBagAndProductIdAndSize(ShoppingBag shoppingBag, String productId, String size);
}