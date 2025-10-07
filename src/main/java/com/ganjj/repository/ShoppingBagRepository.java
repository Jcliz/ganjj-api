package com.ganjj.repository;

import com.ganjj.entities.ShoppingBag;
import com.ganjj.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShoppingBagRepository extends JpaRepository<ShoppingBag, Long> {
    List<ShoppingBag> findByUser(User user);
    
    List<ShoppingBag> findByUserAndStatus(User user, ShoppingBag.ShoppingBagStatus status);
    
    Optional<ShoppingBag> findByUserAndStatusOrderByCreatedAtDesc(User user, ShoppingBag.ShoppingBagStatus status);
}