package com.ganjj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.service.BrandService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

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
public class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandService;

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateBrand_Success() throws Exception {
        BrandCreateDTO createDTO = new BrandCreateDTO();
        createDTO.setName("Nike");
        createDTO.setDescription("Just Do It");
        createDTO.setWebsite("https://www.nike.com");
        createDTO.setCountry("USA");

        Brand brand = createMockBrand(1L, "Nike", "Just Do It", "https://www.nike.com", "USA");
        BrandResponseDTO responseDTO = new BrandResponseDTO(brand);

        when(brandService.createBrand(any(BrandCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/brands/admin")
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Nike"))
                .andExpect(jsonPath("$.description").value("Just Do It"))
                .andExpect(jsonPath("$.website").value("https://www.nike.com"))
                .andExpect(jsonPath("$.country").value("USA"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testCreateBrand_Forbidden() throws Exception {
        BrandCreateDTO createDTO = new BrandCreateDTO();
        createDTO.setName("Nike");

        mockMvc.perform(post("/api/brands/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void testGetActiveBrands_Success() throws Exception {
        Brand brand1Entity = createMockBrand(1L, "Nike", "Just Do It", null, "USA");
        Brand brand2Entity = createMockBrand(2L, "Adidas", "Impossible is Nothing", null, "Germany");

        BrandResponseDTO brand1 = new BrandResponseDTO(brand1Entity);
        BrandResponseDTO brand2 = new BrandResponseDTO(brand2Entity);

        List<BrandResponseDTO> brands = Arrays.asList(brand1, brand2);
        when(brandService.getActiveBrands()).thenReturn(brands);

        mockMvc.perform(get("/api/brands"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Nike"))
                .andExpect(jsonPath("$[1].name").value("Adidas"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateBrand_Success() throws Exception {
        Long brandId = 1L;
        BrandUpdateDTO updateDTO = new BrandUpdateDTO();
        updateDTO.setName("Nike Updated");
        updateDTO.setDescription("Just Do It - Updated");

        Brand brand = createMockBrand(brandId, "Nike Updated", "Just Do It - Updated", null, "USA");
        BrandResponseDTO responseDTO = new BrandResponseDTO(brand);

        when(brandService.updateBrand(eq(brandId), any(BrandUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/brands/admin/{id}", brandId)
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(brandId))
                .andExpect(jsonPath("$.name").value("Nike Updated"))
                .andExpect(jsonPath("$.description").value("Just Do It - Updated"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testUpdateBrand_Forbidden() throws Exception {
        Long brandId = 1L;
        BrandUpdateDTO updateDTO = new BrandUpdateDTO();
        updateDTO.setName("Nike Updated");

        mockMvc.perform(put("/api/brands/admin/{id}", brandId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteBrand_Success() throws Exception {
        Long brandId = 1L;
        doNothing().when(brandService).deleteBrand(brandId);

        mockMvc.perform(delete("/api/brands/admin/{id}", brandId)
                .header("X-Admin-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testDeleteBrand_Forbidden() throws Exception {
        Long brandId = 1L;

        mockMvc.perform(delete("/api/brands/admin/{id}", brandId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateBrand_InvalidInput() throws Exception {
        BrandCreateDTO createDTO = new BrandCreateDTO();
        createDTO.setName("");
        createDTO.setDescription("Test");

        mockMvc.perform(post("/api/brands/admin")
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    private static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Brand createMockBrand(Long id, String name, String description, String website, String country) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setDescription(description);
        brand.setWebsite(website);
        brand.setCountry(country);
        brand.setActive(true);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        return brand;
    }
}
