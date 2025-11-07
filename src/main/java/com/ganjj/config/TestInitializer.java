package com.ganjj.config;

import com.ganjj.entities.*;
import com.ganjj.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.Arrays;

@Configuration
@Profile("test")
public class TestInitializer {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private ShoppingBagRepository shoppingBagRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductReviewRepository reviewRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner loadTestData() {
        return args -> {
            User adminUser = null;
            User regularUser = null;
            
            if (userRepository.count() == 0) {
                adminUser = new User();
                adminUser.setName("Administrador");
                adminUser.setEmail("admin@ganjj.com");
                adminUser.setPassword(passwordEncoder.encode("admin123"));
                adminUser.setRole(User.UserRole.ADMIN);
                adminUser.setPhone("(11) 98765-4321");
                adminUser = userRepository.save(adminUser);

                regularUser = new User();
                regularUser.setName("João Silva");
                regularUser.setEmail("joao@email.com");
                regularUser.setPassword(passwordEncoder.encode("cliente123"));
                regularUser.setRole(User.UserRole.USER);
                regularUser.setPhone("(11) 91234-5678");
                regularUser = userRepository.save(regularUser);

                System.out.println("  → 2 usuários criados");
            } else {
                adminUser = userRepository.findByEmail("admin@ganjj.com").orElse(null);
                regularUser = userRepository.findByEmail("joao@email.com").orElse(null);
            }

            Address addressAdmin = null;
            Address addressUser1 = null;
            Address addressUser2 = null;
            
            if (addressRepository.count() == 0 && regularUser != null) {
                addressAdmin = new Address();
                addressAdmin.setUser(adminUser);
                addressAdmin.setRecipientName("Administrador");
                addressAdmin.setStreet("Av. Paulista");
                addressAdmin.setNumber("1000");
                addressAdmin.setNeighborhood("Bela Vista");
                addressAdmin.setCity("São Paulo");
                addressAdmin.setState("SP");
                addressAdmin.setZipCode("01310-100");
                addressAdmin.setPhone("(11) 98765-4321");
                addressAdmin.setType(Address.AddressType.WORK);
                addressAdmin.setIsDefault(true);
                addressRepository.save(addressAdmin);

                addressUser1 = new Address();
                addressUser1.setUser(regularUser);
                addressUser1.setRecipientName("João Silva");
                addressUser1.setStreet("Rua das Flores");
                addressUser1.setNumber("123");
                addressUser1.setComplement("Apto 45");
                addressUser1.setNeighborhood("Jardim Primavera");
                addressUser1.setCity("São Paulo");
                addressUser1.setState("SP");
                addressUser1.setZipCode("01234-567");
                addressUser1.setPhone("(11) 91234-5678");
                addressUser1.setType(Address.AddressType.HOME);
                addressUser1.setIsDefault(true);
                addressRepository.save(addressUser1);

                addressUser2 = new Address();
                addressUser2.setUser(regularUser);
                addressUser2.setRecipientName("João Silva");
                addressUser2.setStreet("Av. Brasil");
                addressUser2.setNumber("500");
                addressUser2.setNeighborhood("Centro");
                addressUser2.setCity("São Paulo");
                addressUser2.setState("SP");
                addressUser2.setZipCode("98765-432");
                addressUser2.setPhone("(11) 91234-5678");
                addressUser2.setType(Address.AddressType.WORK);
                addressUser2.setIsDefault(false);
                addressRepository.save(addressUser2);

            } else {
                addressUser1 = addressRepository.findByUserIdAndIsDefaultTrue(regularUser.getId()).orElse(null);
            }

            Brand nike = null;
            Brand adidas = null;
            
            if (brandRepository.count() == 0) {
                nike = new Brand();
                nike.setName("Nike");
                nike.setDescription("Just Do It");
                nike.setCountry("Estados Unidos");
                nike.setActive(true);
                brandRepository.save(nike);

                adidas = new Brand();
                adidas.setName("Adidas");
                adidas.setDescription("Impossible is Nothing");
                adidas.setCountry("Alemanha");
                adidas.setActive(true);
                brandRepository.save(adidas);

            } else {
                nike = brandRepository.findByName("Nike").orElse(null);
                adidas = brandRepository.findByName("Adidas").orElse(null);
            }

            Category calcados = null;
            Category roupas = null;
            
            if (categoryRepository.count() == 0) {
                roupas = new Category();
                roupas.setName("Roupas");
                roupas.setDescription("Vestuário em geral");
                roupas.setActive(true);
                categoryRepository.save(roupas);

                calcados = new Category();
                calcados.setName("Calçados");
                calcados.setDescription("Tênis, sapatos e sandálias");
                calcados.setActive(true);
                categoryRepository.save(calcados);

            } else {
                calcados = categoryRepository.findByName("Calçados").orElse(null);
                roupas = categoryRepository.findByName("Roupas").orElse(null);
            }

            Product tenis = null;
            Product camiseta = null;
            
            if (productRepository.count() == 0 && nike != null && calcados != null) {
                tenis = new Product();
                tenis.setName("Tênis Nike Air Max");
                tenis.setDescription("Tênis esportivo com tecnologia Air Max para maior conforto");
                tenis.setPrice(new BigDecimal("499.99"));
                tenis.setStockQuantity(50);
                tenis.setBrand(nike);
                tenis.setCategory(calcados);
                tenis.setActive(true);
                tenis.setFeatured(true);
                tenis.setAvailableSizes(Arrays.asList("38", "39", "40", "41", "42"));
                tenis.setAvailableColors(Arrays.asList("Preto", "Branco", "Azul"));
                tenis.setMaterial("Couro sintético");
                tenis.setDiscountPercent(new BigDecimal("10"));
                productRepository.save(tenis);

                camiseta = new Product();
                camiseta.setName("Camiseta Adidas Performance");
                camiseta.setDescription("Camiseta esportiva de alta performance");
                camiseta.setPrice(new BigDecimal("89.90"));
                camiseta.setStockQuantity(100);
                camiseta.setBrand(adidas);
                camiseta.setCategory(roupas);
                camiseta.setActive(true);
                camiseta.setFeatured(false);
                camiseta.setAvailableSizes(Arrays.asList("P", "M", "G", "GG"));
                camiseta.setAvailableColors(Arrays.asList("Preto", "Branco"));
                camiseta.setMaterial("Poliéster");
                productRepository.save(camiseta);

            } else {
                tenis = productRepository.findAll().stream().findFirst().orElse(null);
                camiseta = productRepository.findAll().stream().skip(1).findFirst().orElse(null);
            }

            ShoppingBag shoppingBag = null;
            
            if (shoppingBagRepository.count() == 0 && regularUser != null && tenis != null) {
                shoppingBag = new ShoppingBag();
                shoppingBag.setUser(regularUser);
                shoppingBag.setStatus(ShoppingBag.ShoppingBagStatus.OPEN);
                shoppingBagRepository.save(shoppingBag);

                ShoppingBagItem item1 = new ShoppingBagItem();
                item1.setProductId(tenis.getId().toString());
                item1.setProductName(tenis.getName());
                item1.setProductImage("nike-air-max.jpg");
                item1.setSize("40");
                item1.setQuantity(1);
                item1.setPrice(tenis.getCurrentPrice());
                shoppingBag.addItem(item1);

                if (camiseta != null) {
                    ShoppingBagItem item2 = new ShoppingBagItem();
                    item2.setProductId(camiseta.getId().toString());
                    item2.setProductName(camiseta.getName());
                    item2.setProductImage("adidas-performance.jpg");
                    item2.setSize("M");
                    item2.setQuantity(2);
                    item2.setPrice(camiseta.getPrice());
                    shoppingBag.addItem(item2);
                }

                shoppingBagRepository.save(shoppingBag);

            }

            if (orderRepository.count() == 0 && regularUser != null && addressUser1 != null && tenis != null) {
                Order order = new Order();
                order.setUser(regularUser);
                order.setDeliveryAddress(addressUser1);
                order.setPaymentMethod(Order.PaymentMethod.CREDIT_CARD);
                order.setPaymentStatus(Order.PaymentStatus.PAID);
                order.setStatus(Order.OrderStatus.CONFIRMED);
                
                order.setDeliveryStreet(addressUser1.getStreet());
                order.setDeliveryNumber(addressUser1.getNumber());
                order.setDeliveryComplement(addressUser1.getComplement());
                order.setDeliveryNeighborhood(addressUser1.getNeighborhood());
                order.setDeliveryCity(addressUser1.getCity());
                order.setDeliveryState(addressUser1.getState());
                order.setDeliveryZipCode(addressUser1.getZipCode());

                OrderItem orderItem = new OrderItem();
                orderItem.setProduct(tenis);
                orderItem.setProductName(tenis.getName());
                orderItem.setProductImage("nike-air-max.jpg");
                orderItem.setSize("40");
                orderItem.setColor("Preto");
                orderItem.setQuantity(1);
                orderItem.setUnitPrice(tenis.getPrice());
                orderItem.setDiscountPercent(tenis.getDiscountPercent());
                
                order.addItem(orderItem);
                order.recalculateTotalAmount();
                
                orderRepository.save(order);

            }

            if (reviewRepository.count() == 0 && regularUser != null && tenis != null) {
                ProductReview review = new ProductReview();
                review.setUser(regularUser);
                review.setProduct(tenis);
                review.setRating(5);
                review.setComment("Excelente tênis! Muito confortável e bonito.");
                review.setVerifiedPurchase(true);
                review.setActive(true);
                reviewRepository.save(review);
            }
        };
    }
}

