package com.ganjj.service;

import com.ganjj.dto.OrderCreateDTO;
import com.ganjj.dto.OrderResponseDTO;
import com.ganjj.dto.OrderUpdateStatusDTO;
import com.ganjj.entities.*;
import com.ganjj.exception.ConflictException;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.*;
import com.ganjj.security.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long authenticatedUserId = userDetails.getId();
        
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        
        if (!isAdmin && !authenticatedUserId.equals(createDTO.getUserId())) {
            throw new com.ganjj.exception.AccessDeniedException(ErrorCode.AUTH_ACCESS_DENIED);
        }
        
        User user = userRepository.findById(createDTO.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, createDTO.getUserId()));

        ShoppingBag shoppingBag = shoppingBagRepository.findById(createDTO.getShoppingBagId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.SHOPPING_BAG_NOT_FOUND, createDTO.getShoppingBagId()));

        if (!shoppingBag.getUser().getId().equals(user.getId())) {
            throw new ValidationException(ErrorCode.ORDER_BAG_NOT_BELONGS_TO_USER);
        }

        if (shoppingBag.getItems().isEmpty()) {
            throw new ValidationException(ErrorCode.ORDER_BAG_IS_EMPTY);
        }

        if (!shoppingBag.getStatus().equals(ShoppingBag.ShoppingBagStatus.OPEN)) {
            throw new ValidationException(ErrorCode.ORDER_BAG_ALREADY_FINALIZED);
        }

        Address address = addressRepository.findById(createDTO.getAddressId())
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ADDRESS_NOT_FOUND, createDTO.getAddressId()));

        if (!address.getUser().getId().equals(user.getId())) {
            throw new ValidationException(ErrorCode.ORDER_ADDRESS_NOT_BELONGS_TO_USER);
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
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, bagItem.getProductId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setProductName(bagItem.getProductName());
            orderItem.setSize(bagItem.getSize());
            orderItem.setQuantity(bagItem.getQuantity());
            orderItem.setUnitPrice(bagItem.getPrice());
            orderItem.calculateSubtotal();

            order.addItem(orderItem);

            if (product.getStockQuantity() < bagItem.getQuantity()) {
                throw new ConflictException(ErrorCode.ORDER_INSUFFICIENT_STOCK, product.getName());
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, id));
        return new OrderResponseDTO(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponseDTO> getUserOrders(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(ErrorCode.USER_NOT_FOUND, userId);
        }
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId).stream()
                .map(OrderResponseDTO::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponseDTO updateOrderStatus(Long id, OrderUpdateStatusDTO updateDTO) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND, id));

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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == Order.OrderStatus.DELIVERED || 
            order.getStatus() == Order.OrderStatus.SHIPPED) {
            throw new ConflictException(ErrorCode.ORDER_CANNOT_DELETE_SENT_OR_DELIVERED);
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
