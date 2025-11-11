package com.ganjj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganjj.dto.ProductCreateDTO;
import com.ganjj.dto.ProductResponseDTO;
import com.ganjj.dto.ProductUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import com.ganjj.service.ProductService;
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
public class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateProduct_Success() throws Exception {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Tênis Nike Air Max");
        createDTO.setDescription("Tênis esportivo de alta qualidade");
        createDTO.setPrice(new BigDecimal("299.99"));
        createDTO.setStockQuantity(50);
        createDTO.setBrandId(1L);
        createDTO.setCategoryId(1L);

        Product product = createMockProduct(1L, "Tênis Nike Air Max", "Tênis esportivo de alta qualidade", 
                                            new BigDecimal("299.99"), 50);
        ProductResponseDTO responseDTO = new ProductResponseDTO(product);

        when(productService.createProduct(any(ProductCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/products")
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Tênis Nike Air Max"))
                .andExpect(jsonPath("$.price").value(299.99))
                .andExpect(jsonPath("$.stockQuantity").value(50))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testCreateProduct_Forbidden() throws Exception {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Produto Teste");
        createDTO.setPrice(new BigDecimal("100.00"));
        createDTO.setStockQuantity(10);
        createDTO.setBrandId(1L);
        createDTO.setCategoryId(1L);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllProducts_Success() throws Exception {
        Product product1 = createMockProduct(1L, "Produto 1", "Descrição 1", new BigDecimal("100.00"), 10);
        Product product2 = createMockProduct(2L, "Produto 2", "Descrição 2", new BigDecimal("200.00"), 20);

        ProductResponseDTO productDTO1 = new ProductResponseDTO(product1);
        ProductResponseDTO productDTO2 = new ProductResponseDTO(product2);

        List<ProductResponseDTO> products = Arrays.asList(productDTO1, productDTO2);
        when(productService.getAllProductsList()).thenReturn(products);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Produto 1"))
                .andExpect(jsonPath("$[1].name").value("Produto 2"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateProduct_Success() throws Exception {
        Long productId = 1L;
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Produto Atualizado");
        updateDTO.setPrice(new BigDecimal("350.00"));
        updateDTO.setStockQuantity(75);

        Product product = createMockProduct(productId, "Produto Atualizado", "Descrição atualizada", 
                                            new BigDecimal("350.00"), 75);
        ProductResponseDTO responseDTO = new ProductResponseDTO(product);

        when(productService.updateProduct(eq(productId), any(ProductUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/products/{id}", productId)
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId))
                .andExpect(jsonPath("$.name").value("Produto Atualizado"))
                .andExpect(jsonPath("$.price").value(350.00))
                .andExpect(jsonPath("$.stockQuantity").value(75));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testUpdateProduct_Forbidden() throws Exception {
        Long productId = 1L;
        ProductUpdateDTO updateDTO = new ProductUpdateDTO();
        updateDTO.setName("Produto Atualizado");

        mockMvc.perform(put("/api/products/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteProduct_Success() throws Exception {
        Long productId = 1L;
        doNothing().when(productService).deleteProduct(productId);

        mockMvc.perform(delete("/api/products/{id}", productId)
                .header("X-Admin-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testDeleteProduct_Forbidden() throws Exception {
        Long productId = 1L;

        mockMvc.perform(delete("/api/products/{id}", productId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateProduct_InvalidInput() throws Exception {
        ProductCreateDTO createDTO = new ProductCreateDTO();
        createDTO.setName("Produto");
        createDTO.setPrice(new BigDecimal("-10.00"));
        createDTO.setStockQuantity(10);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetAllProducts_EmptyList() throws Exception {
        when(productService.getAllProductsList()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/products"))
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

    private Product createMockProduct(Long id, String name, String description, BigDecimal price, Integer stock) {
        Product product = new Product();
        product.setId(id);
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stock);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());

        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("Nike");
        brand.setActive(true);
        product.setBrand(brand);

        Category category = new Category();
        category.setId(1L);
        category.setName("Calçados");
        category.setActive(true);
        product.setCategory(category);

        return product;
    }
}
