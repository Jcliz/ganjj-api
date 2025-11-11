package com.ganjj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganjj.dto.OrderCreateDTO;
import com.ganjj.dto.OrderResponseDTO;
import com.ganjj.dto.OrderUpdateStatusDTO;
import com.ganjj.entities.Order;
import com.ganjj.entities.User;
import com.ganjj.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    @Test
    @WithMockUser(roles = {"USER"})
    public void testCreateOrder_Success() throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setShoppingBagId(1L);
        createDTO.setAddressId(1L);
        createDTO.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);

        Order order = createMockOrder(1L, 1L, Order.OrderStatus.PENDING, new BigDecimal("299.98"));
        OrderResponseDTO responseDTO = new OrderResponseDTO(order);

        when(orderService.createOrder(any(OrderCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(299.98));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateOrder_AsAdmin_Success() throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setShoppingBagId(1L);
        createDTO.setAddressId(1L);
        createDTO.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);

        Order order = createMockOrder(1L, 1L, Order.OrderStatus.PENDING, new BigDecimal("149.99"));
        OrderResponseDTO responseDTO = new OrderResponseDTO(order);

        when(orderService.createOrder(any(OrderCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetAllOrders_Success() throws Exception {
        Order order1 = createMockOrder(1L, 1L, Order.OrderStatus.PENDING, new BigDecimal("299.98"));
        Order order2 = createMockOrder(2L, 2L, Order.OrderStatus.DELIVERED, new BigDecimal("499.99"));

        OrderResponseDTO orderDTO1 = new OrderResponseDTO(order1);
        OrderResponseDTO orderDTO2 = new OrderResponseDTO(order2);

        List<OrderResponseDTO> orders = Arrays.asList(orderDTO1, orderDTO2);
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].status").value("PENDING"))
                .andExpect(jsonPath("$[1].status").value("DELIVERED"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testGetAllOrders_Forbidden() throws Exception {
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testGetOrderById_Success() throws Exception {
        Long orderId = 1L;
        Order order = createMockOrder(orderId, 1L, Order.OrderStatus.PENDING, new BigDecimal("299.98"));
        OrderResponseDTO responseDTO = new OrderResponseDTO(order);

        when(orderService.getOrderById(orderId)).thenReturn(responseDTO);

        mockMvc.perform(get("/api/orders/{id}", orderId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(299.98));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateOrderStatus_Success() throws Exception {
        Long orderId = 1L;
        OrderUpdateStatusDTO updateDTO = new OrderUpdateStatusDTO();
        updateDTO.setOrderStatus(Order.OrderStatus.SHIPPED);

        Order order = createMockOrder(orderId, 1L, Order.OrderStatus.SHIPPED, new BigDecimal("299.98"));
        OrderResponseDTO responseDTO = new OrderResponseDTO(order);

        when(orderService.updateOrderStatus(eq(orderId), any(OrderUpdateStatusDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId))
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testUpdateOrderStatus_Forbidden() throws Exception {
        Long orderId = 1L;
        OrderUpdateStatusDTO updateDTO = new OrderUpdateStatusDTO();
        updateDTO.setOrderStatus(Order.OrderStatus.SHIPPED);

        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteOrder_Success() throws Exception {
        Long orderId = 1L;
        doNothing().when(orderService).deleteOrder(orderId);

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testDeleteOrder_Forbidden() throws Exception {
        Long orderId = 1L;

        mockMvc.perform(delete("/api/orders/{id}", orderId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testCreateOrder_InvalidInput() throws Exception {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);

        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetAllOrders_EmptyList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Order createMockOrder(Long id, Long userId, Order.OrderStatus status, BigDecimal totalAmount) {
        Order order = new Order();
        order.setId(id);
        
        User user = new User();
        user.setId(userId);
        order.setUser(user);
        
        order.setStatus(status);
        order.setTotalAmount(totalAmount);
        order.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        
        return order;
    }
}
