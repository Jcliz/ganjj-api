package com.ganjj.service;

import com.ganjj.dto.OrderCreateDTO;
import com.ganjj.dto.OrderResponseDTO;
import com.ganjj.dto.OrderUpdateStatusDTO;
import com.ganjj.entities.*;
import com.ganjj.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShoppingBagRepository shoppingBagRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public OrderResponseDTO createOrder(OrderCreateDTO createDTO) {
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado com o ID: " + createDTO.getUserId()));

        ShoppingBag shoppingBag = shoppingBagRepository.findById(createDTO.getShoppingBagId())
                .orElseThrow(() -> new EntityNotFoundException("Sacola não encontrada com o ID: " + createDTO.getShoppingBagId()));

        if (!shoppingBag.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("A sacola não pertence ao usuário informado.");
        }

        if (shoppingBag.getItems().isEmpty()) {
            throw new IllegalArgumentException("A sacola está vazia. Não é possível criar um pedido.");
        }

        if (!shoppingBag.getStatus().equals(ShoppingBag.ShoppingBagStatus.OPEN)) {
            throw new IllegalArgumentException("A sacola já foi finalizada.");
        }

        Address address = addressRepository.findById(createDTO.getAddressId())
                .orElseThrow(() -> new EntityNotFoundException("Endereço não encontrado com o ID: " + createDTO.getAddressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("O endereço não pertence ao usuário informado.");
        }

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(createDTO.getPaymentMethod());
        order.setNotes(createDTO.getNotes());
        order.setDeliveryAddress(address);

        order.setDeliveryStreet(address.getStreet());
        order.setDeliveryNumber(address.getNumber());
        order.setDeliveryComplement(address.getComplement());
        order.setDeliveryNeighborhood(address.getNeighborhood());
        order.setDeliveryCity(address.getCity());
        order.setDeliveryState(address.getState());
        order.setDeliveryZipCode(address.getZipCode());

        for (ShoppingBagItem bagItem : shoppingBag.getItems()) {
            Product product = productRepository.findById(Long.parseLong(bagItem.getProductId()))
                    .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + bagItem.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(bagItem.getProductName());
            orderItem.setSize(bagItem.getSize());
            orderItem.setQuantity(bagItem.getQuantity());
            orderItem.setUnitPrice(bagItem.getPrice());
            orderItem.setDiscountPercent(product.getDiscountPercent());
            orderItem.calculateSubtotal();

            order.addItem(orderItem);

            if (product.getStockQuantity() < bagItem.getQuantity()) {
                throw new IllegalStateException("Estoque insuficiente para o produto: " + product.getName());
            }
            product.setStockQuantity(product.getStockQuantity() - bagItem.getQuantity());
            productRepository.save(product);
        }

        order.recalculateTotalAmount();

        shoppingBag.setStatus(ShoppingBag.ShoppingBagStatus.COMPLETED);
        shoppingBagRepository.save(shoppingBag);

        Order savedOrder = orderRepository.save(order);

        return new OrderResponseDTO(savedOrder);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderResponseDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));
        return new OrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("Usuário não encontrado com o ID: " + userId);
        }
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderUpdateStatusDTO updateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));

        if (updateDTO.getOrderStatus() != null) {
            order.setStatus(updateDTO.getOrderStatus());

            switch (updateDTO.getOrderStatus()) {
                case CONFIRMED:
                    break;
                case SHIPPED:
                    order.setShippedDate(LocalDateTime.now());
                    break;
                case DELIVERED:
                    order.setDeliveredDate(LocalDateTime.now());
                    break;
                case CANCELLED:
                    order.setCancelledDate(LocalDateTime.now());
                    returnStockOnCancellation(order);
                    break;
                default:
                    break;
            }
        }

        if (updateDTO.getPaymentStatus() != null) {
            order.setPaymentStatus(updateDTO.getPaymentStatus());
            if (updateDTO.getPaymentStatus() == Order.PaymentStatus.PAID) {
                order.setPaymentDate(LocalDateTime.now());
            }
        }

        if (updateDTO.getTrackingCode() != null) {
            order.setTrackingCode(updateDTO.getTrackingCode());
        }

        Order updatedOrder = orderRepository.save(order);
        return new OrderResponseDTO(updatedOrder);
    }

    private void returnStockOnCancellation(Order order) {
        for (OrderItem item : order.getItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
    }

    @Transactional
    public void deleteOrder(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Pedido não encontrado com o ID: " + id));

        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new IllegalStateException("Não é possível excluir um pedido que já foi enviado ou entregue.");
        }

        if (order.getStatus() != Order.OrderStatus.CANCELLED) {
            returnStockOnCancellation(order);
        }

        orderRepository.delete(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }
}
