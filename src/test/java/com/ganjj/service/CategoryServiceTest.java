package com.ganjj.service;

import com.ganjj.dto.CategoryCreateDTO;
import com.ganjj.dto.CategoryResponseDTO;
import com.ganjj.dto.CategoryUpdateDTO;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import com.ganjj.exception.ConflictException;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

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
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @MockitoBean
    private CategoryRepository categoryRepository;

    @Test
    void createCategory_shouldCreateSuccessfully() {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setName("Roupas");
        createDTO.setDescription("Roupas em geral");

        Category savedCategory = new Category();
        savedCategory.setId(1L);
        savedCategory.setName("Roupas");
        savedCategory.setDescription("Roupas em geral");
        savedCategory.setActive(true);
        savedCategory.setCreatedAt(LocalDateTime.now());
        savedCategory.setUpdatedAt(LocalDateTime.now());

        when(categoryRepository.findByName("Roupas")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

        CategoryResponseDTO result = categoryService.createCategory(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Roupas");
        assertThat(result.getDescription()).isEqualTo("Roupas em geral");
        assertThat(result.getActive()).isTrue();

        verify(categoryRepository).findByName("Roupas");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_shouldThrowException_whenNameAlreadyExists() {
        CategoryCreateDTO createDTO = new CategoryCreateDTO();
        createDTO.setName("Roupas");

        Category existingCategory = new Category();
        existingCategory.setName("Roupas");

        when(categoryRepository.findByName("Roupas")).thenReturn(Optional.of(existingCategory));

        assertThatThrownBy(() -> categoryService.createCategory(createDTO))
                .isInstanceOf(ValidationException.class);

        verify(categoryRepository).findByName("Roupas");
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void getActiveCategories_shouldReturnActiveCategories() {
        Category category1 = createMockCategory(1L, "Roupas", true);
        Category category2 = createMockCategory(2L, "Calçados", true);

        when(categoryRepository.findByActive(true))
                .thenReturn(Arrays.asList(category1, category2));

        List<CategoryResponseDTO> result = categoryService.getActiveCategories();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Roupas");
        assertThat(result.get(1).getName()).isEqualTo("Calçados");

        verify(categoryRepository).findByActive(true);
    }

    @Test
    void getCategoryById_shouldReturnCategory() {
        Category category = createMockCategory(1L, "Roupas", true);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        CategoryResponseDTO result = categoryService.getCategoryById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Roupas");

        verify(categoryRepository).findById(1L);
    }

    @Test
    void getCategoryById_shouldThrowException_whenNotFound() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository).findById(999L);
    }

    @Test
    void updateCategory_shouldUpdateSuccessfully() {
        Category existingCategory = createMockCategory(1L, "Roupas", true);
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setName("Roupas Updated");
        updateDTO.setDescription("Nova descrição");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName("Roupas Updated")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        CategoryResponseDTO result = categoryService.updateCategory(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(categoryRepository).findById(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_shouldThrowException_whenNewNameAlreadyExists() {
        Category existingCategory = createMockCategory(1L, "Roupas", true);
        Category anotherCategory = createMockCategory(2L, "Calçados", true);
        
        CategoryUpdateDTO updateDTO = new CategoryUpdateDTO();
        updateDTO.setName("Calçados");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.findByName("Calçados")).thenReturn(Optional.of(anotherCategory));

        assertThatThrownBy(() -> categoryService.updateCategory(1L, updateDTO))
                .isInstanceOf(ValidationException.class);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).save(any(Category.class));
    }

    @Test
    void deleteCategory_shouldDeleteSuccessfully() {
        Category category = createMockCategory(1L, "Roupas", true);
        category.setProducts(new ArrayList<>());

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        doNothing().when(categoryRepository).delete(category);

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void deleteCategory_shouldThrowException_whenHasProducts() {
        Category category = createMockCategory(1L, "Roupas", true);
        List<Product> products = new ArrayList<>();
        products.add(new Product());
        category.setProducts(products);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        assertThatThrownBy(() -> categoryService.deleteCategory(1L))
                .isInstanceOf(ConflictException.class);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository, never()).delete(any(Category.class));
    }

    @Test
    void getAllCategories_shouldReturnAllCategories() {
        Category category1 = createMockCategory(1L, "Roupas", true);
        Category category2 = createMockCategory(2L, "Calçados", false);

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(category1, category2));

        List<CategoryResponseDTO> result = categoryService.getAllCategories();

        assertThat(result).hasSize(2);
        verify(categoryRepository).findAll();
    }

    private Category createMockCategory(Long id, String name, Boolean active) {
        Category category = new Category();
        category.setId(id);
        category.setName(name);
        category.setDescription("Description for " + name);
        category.setActive(active);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setProducts(new ArrayList<>());
        return category;
    }
}
