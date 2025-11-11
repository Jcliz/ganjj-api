package com.ganjj.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ganjj.dto.CategoryCreateDTO;
import com.ganjj.dto.CategoryResponseDTO;
import com.ganjj.dto.CategoryUpdateDTO;
import com.ganjj.entities.Category;
import com.ganjj.service.CategoryService;
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
public class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    public void testGetActiveCategories_Success() throws Exception {
        Category cat1Entity = createMockCategory(1L, "Roupas", "Roupas em geral");
        Category cat2Entity = createMockCategory(2L, "Calçados", "Sapatos e tênis");

        CategoryResponseDTO category1 = new CategoryResponseDTO(cat1Entity);
        CategoryResponseDTO category2 = new CategoryResponseDTO(cat2Entity);

        List<CategoryResponseDTO> categories = Arrays.asList(category1, category2);
        when(categoryService.getActiveCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Roupas"))
                .andExpect(jsonPath("$[1].name").value("Calçados"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateCategory_Success() throws Exception {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setName("Roupas");
        createDTO.setDescription("Roupas em geral");

        Category category = createMockCategory(1L, "Roupas", "Roupas em geral");
        CategoryResponseDTO responseDTO = new CategoryResponseDTO(category);

        when(categoryService.createCategory(any(CategoryCreateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/categories/admin")
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Roupas"))
                .andExpect(jsonPath("$.description").value("Roupas em geral"))
                .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testCreateCategory_Forbidden() throws Exception {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setName("Roupas");

        mockMvc.perform(post("/api/categories/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateCategory_Success() throws Exception {
        Long categoryId = 1L;
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setName("Roupas Updated");
        updateDTO.setDescription("Nova descrição");

        Category category = createMockCategory(categoryId, "Roupas Updated", "Nova descrição");
        CategoryResponseDTO responseDTO = new CategoryResponseDTO(category);

        when(categoryService.updateCategory(eq(categoryId), any(CategoryUpdateDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(put("/api/categories/admin/{id}", categoryId)
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("Roupas Updated"))
                .andExpect(jsonPath("$.description").value("Nova descrição"));
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testUpdateCategory_Forbidden() throws Exception {
        Long categoryId = 1L;
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setName("Roupas Updated");

        mockMvc.perform(put("/api/categories/admin/{id}", categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(updateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteCategory_Success() throws Exception {
        Long categoryId = 1L;
        doNothing().when(categoryService).deleteCategory(categoryId);

        mockMvc.perform(delete("/api/categories/admin/{id}", categoryId)
                .header("X-Admin-Role", "ADMIN"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"USER"})
    public void testDeleteCategory_Forbidden() throws Exception {
        Long categoryId = 1L;

        mockMvc.perform(delete("/api/categories/admin/{id}", categoryId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateCategory_InvalidInput() throws Exception {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setName("");
        createDTO.setDescription("Test");

        mockMvc.perform(post("/api/categories/admin")
                .header("X-Admin-Role", "ADMIN")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(createDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetActiveCategories_EmptyList() throws Exception {
        when(categoryService.getActiveCategories()).thenReturn(Arrays.asList());

        mockMvc.perform(get("/api/categories"))
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

    private Category createMockCategory(Long id, String name, String description) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription(description);
        category.setActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        return category;
    }
}
