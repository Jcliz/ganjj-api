package com.ganjj.repository;

import com.ganjj.entities.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);

    List<Order> findByStatus(Order.OrderStatus status);

    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);

    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    List<Order> findByUserIdAndStatus(Long userId, Order.OrderStatus status);
}
