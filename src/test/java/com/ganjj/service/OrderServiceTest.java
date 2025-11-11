package com.ganjj.service;

import com.ganjj.dto.OrderCreateDTO;
import com.ganjj.dto.OrderResponseDTO;
import com.ganjj.dto.OrderUpdateStatusDTO;
import com.ganjj.entities.*;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.*;
import com.ganjj.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@Transactional
@TestPropertySource(properties = "spring.profiles.active=service-test")
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private ShoppingBagRepository shoppingBagRepository;

    @MockitoBean
    private AddressRepository addressRepository;

    @MockitoBean
    private ProductRepository productRepository;

    private User mockUser;
    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setRole(User.UserRole.USER);

        userDetails = new UserDetailsImpl(
                1L,
                "Test User",
                "test@example.com",
                "password",
                Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createOrder_shouldCreateSuccessfully() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setShoppingBagId(1L);
        createDTO.setAddressId(1L);
        createDTO.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);

        User user = createMockUser(1L, "test@example.com");
        Address address = createMockAddress(1L, user);
        ShoppingBag shoppingBag = createMockShoppingBag(1L, user);
        Product product = createMockProduct(1L, "Product", new BigDecimal("100.00"));

        ShoppingBagItem bagItem = new ShoppingBagItem();
        bagItem.setProductId("1");
        bagItem.setProductName("Product");
        bagItem.setQuantity(2);
        bagItem.setPrice(new BigDecimal("100.00"));
        shoppingBag.setItems(Arrays.asList(bagItem));

        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setUser(user);
        savedOrder.setStatus(Order.OrderStatus.PENDING);
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setItems(new ArrayList<>());
        savedOrder.setCreatedAt(LocalDateTime.now());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shoppingBagRepository.findById(1L)).thenReturn(Optional.of(shoppingBag));
        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);

        OrderResponseDTO result = orderService.createOrder(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getUserId()).isEqualTo(1L);

        verify(userRepository).findById(1L);
        verify(shoppingBagRepository).findById(1L);
        verify(addressRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowException_whenUserNotFound() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setShoppingBagId(1L);
        createDTO.setAddressId(1L);
        createDTO.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.createOrder(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void createOrder_shouldThrowException_whenShoppingBagIsEmpty() {
        OrderCreateDTO createDTO = new OrderCreateDTO();
        createDTO.setUserId(1L);
        createDTO.setShoppingBagId(1L);
        createDTO.setAddressId(1L);
        createDTO.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);

        User user = createMockUser(1L, "test@example.com");
        ShoppingBag shoppingBag = createMockShoppingBag(1L, user);
        shoppingBag.setItems(new ArrayList<>());

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(shoppingBagRepository.findById(1L)).thenReturn(Optional.of(shoppingBag));

        assertThatThrownBy(() -> orderService.createOrder(createDTO))
                .isInstanceOf(ValidationException.class);

        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getAllOrders_shouldReturnAllOrders() {
        User user = createMockUser(1L, "test@example.com");
        Order order1 = createMockOrder(1L, user, Order.OrderStatus.PENDING);
        Order order2 = createMockOrder(2L, user, Order.OrderStatus.DELIVERED);

        when(orderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        List<OrderResponseDTO> result = orderService.getAllOrders();

        assertThat(result).hasSize(2);
        verify(orderRepository).findAll();
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        User user = createMockUser(1L, "test@example.com");
        Order order = createMockOrder(1L, user, Order.OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderResponseDTO result = orderService.getOrderById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(orderRepository).findById(1L);
    }

    @Test
    void getOrderById_shouldThrowException_whenNotFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(orderRepository).findById(999L);
    }

    @Test
    void updateOrderStatus_shouldUpdateSuccessfully() {
        User user = createMockUser(1L, "test@example.com");
        Order order = createMockOrder(1L, user, Order.OrderStatus.PENDING);
        OrderUpdateStatusDTO updateDTO = new OrderUpdateStatusDTO();
        updateDTO.setOrderStatus(Order.OrderStatus.SHIPPED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        OrderResponseDTO result = orderService.updateOrderStatus(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(orderRepository).findById(1L);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void deleteOrder_shouldDeleteSuccessfully() {
        User user = createMockUser(1L, "test@example.com");
        Order order = createMockOrder(1L, user, Order.OrderStatus.PENDING);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        doNothing().when(orderRepository).delete(order);

        orderService.deleteOrder(1L);

        verify(orderRepository).findById(1L);
        verify(orderRepository).delete(order);
    }

    private User createMockUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setRole(User.UserRole.USER);
        return user;
    }

    private Address createMockAddress(Long id, User user) {
        Address address = new Address();
        address.setId(id);
        address.setUser(user);
        address.setStreet("Test Street");
        address.setNumber("123");
        address.setCity("Test City");
        address.setState("TS");
        address.setZipCode("12345-678");
        return address;
    }

    private ShoppingBag createMockShoppingBag(Long id, User user) {
        ShoppingBag bag = new ShoppingBag();
        bag.setId(id);
        bag.setUser(user);
        bag.setStatus(ShoppingBag.ShoppingBagStatus.OPEN);
        bag.setItems(new ArrayList<>());
        return bag;
    }

    private Product createMockProduct(Long id, String name, BigDecimal price) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setPrice(price);
        product.setStockQuantity(100);
        product.setActive(true);
        return product;
    }

    private Order createMockOrder(Long id, User user, Order.OrderStatus status) {
        Order order = new Order();
        order.setId(id);
        order.setUser(user);
        order.setStatus(status);
        order.setTotalAmount(new BigDecimal("200.00"));
        order.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);
        order.setPaymentStatus(Order.PaymentStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setCreatedAt(LocalDateTime.now());
        order.setItems(new ArrayList<>());
        return order;
    }
}
