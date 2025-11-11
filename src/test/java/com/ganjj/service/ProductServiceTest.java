package com.ganjj.service;

import com.ganjj.dto.ProductCreateDTO;
import com.ganjj.dto.ProductResponseDTO;
import com.ganjj.dto.ProductUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.BrandRepository;
import com.ganjj.repository.CategoryRepository;
import com.ganjj.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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
class ProductServiceTest {

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private BrandRepository brandRepository;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    void createProduct_shouldCreateSuccessfully() {
         Arrange
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Tênis Nike Air Max");
        createDTO.setDescription("Tênis esportivo");
        createDTO.setPrice(new BigDecimal("299.99"));
        createDTO.setStockQuantity(50);
        createDTO.setBrandId(1L);
        createDTO.setCategoryId(1L);

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Nike");

        Category category = new Category();
        category.setId(1L);
        category.setName("Calçados");

        Product savedProduct = new Product();
        savedProduct.setId(1L);
        savedProduct.setName("Tênis Nike Air Max");
        savedProduct.setDescription("Tênis esportivo");
        savedProduct.setPrice(new BigDecimal("299.99"));
        savedProduct.setStockQuantity(50);
        savedProduct.setBrand(brand);
        savedProduct.setCategory(category);
        savedProduct.setActive(true);
        savedProduct.setCreatedAt(LocalDateTime.now());
        savedProduct.setUpdatedAt(LocalDateTime.now());

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

         Act
        ProductResponseDTO result = productService.createProduct(createDTO);

         Assert
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tênis Nike Air Max");
        assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("299.99"));
        assertThat(result.getStockQuantity()).isEqualTo(50);
        assertThat(result.getActive()).isTrue();

        verify(brandRepository).findById(1L);
        verify(categoryRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenPriceIsInvalid() {
         Arrange
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Produto");
        createDTO.setPrice(new BigDecimal("-10.00"));
        createDTO.setStockQuantity(10);

         Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createDTO))
                .isInstanceOf(ValidationException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenStockIsNegative() {
         Arrange
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Produto");
        createDTO.setPrice(new BigDecimal("100.00"));
        createDTO.setStockQuantity(-5);

         Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createDTO))
                .isInstanceOf(ValidationException.class);

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void createProduct_shouldThrowException_whenBrandNotFound() {
         Arrange
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Produto");
        createDTO.setPrice(new BigDecimal("100.00"));
        createDTO.setStockQuantity(10);
        createDTO.setBrandId(999L);

        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

         Act & Assert
        assertThatThrownBy(() -> productService.createProduct(createDTO))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(brandRepository).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getAllProductsList_shouldReturnAllProducts() {
         Arrange
        Product product1 = createMockProduct(1L, "Produto 1", new BigDecimal("100.00"), 10);
        Product product2 = createMockProduct(2L, "Produto 2", new BigDecimal("200.00"), 20);

        when(productRepository.findAll()).thenReturn(Arrays.asList(product1, product2));

         Act
        List<ProductResponseDTO> result = productService.getAllProductsList();

         Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Produto 1");
        assertThat(result.get(1).getName()).isEqualTo("Produto 2");

        verify(productRepository).findAll();
    }

    @Test
    void getProductById_shouldReturnProduct() {
         Arrange
        Product product = createMockProduct(1L, "Produto", new BigDecimal("100.00"), 10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

         Act
        ProductResponseDTO result = productService.getProductById(1L);

         Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Produto");

        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_shouldThrowException_whenNotFound() {
         Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

         Act & Assert
        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository).findById(999L);
    }

    @Test
    void updateProduct_shouldUpdateSuccessfully() {
         Arrange
        Product existingProduct = createMockProduct(1L, "Produto", new BigDecimal("100.00"), 10);
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Produto Updated");
        updateDTO.setPrice(new BigDecimal("150.00"));
        updateDTO.setStockQuantity(20);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.save(any(Product.class))).thenReturn(existingProduct);

         Act
        ProductResponseDTO result = productService.updateProduct(1L, updateDTO);

         Assert
        assertThat(result).isNotNull();
        verify(productRepository).findById(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_shouldDeleteSuccessfully() {
         Arrange
        when(productRepository.existsById(1L)).thenReturn(true);
        doNothing().when(productRepository).deleteById(1L);

         Act
        productService.deleteProduct(1L);

         Assert
        verify(productRepository).existsById(1L);
        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_shouldThrowException_whenNotFound() {
         Arrange
        when(productRepository.existsById(999L)).thenReturn(false);

         Act & Assert
        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository).existsById(999L);
        verify(productRepository, never()).deleteById(anyLong());
    }

    private Product createMockProduct(Long id, String name, BigDecimal price, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription("Description for " + name);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Test Brand");
        product.setBrand(brand);

        Category category = new Category();
        category.setId(1L);
        category.setName("Test Category");
        product.setCategory(category);

        return product;
    }
}
