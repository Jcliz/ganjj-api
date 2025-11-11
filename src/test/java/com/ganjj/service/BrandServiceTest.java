package com.ganjj.service;

import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.BrandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

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
class BrandServiceTest {

    @Autowired
    private BrandService brandService;

    @MockitoBean
    private BrandRepository brandRepository;

    @Test
    void createBrand_shouldCreateSuccessfully() {
        BrandCreateDTO createDTO = new BrandCreateDTO();
        createDTO.setName("Nike");
        createDTO.setDescription("Just Do It");
        createDTO.setWebsite("https://www.nike.com");
        createDTO.setCountry("USA");

        Brand savedBrand = new Brand();
        savedBrand.setId(1L);
        savedBrand.setName("Nike");
        savedBrand.setDescription("Just Do It");
        savedBrand.setWebsite("https://www.nike.com");
        savedBrand.setCountry("USA");
        savedBrand.setActive(true);
        savedBrand.setCreatedAt(LocalDateTime.now());
        savedBrand.setUpdatedAt(LocalDateTime.now());

        when(brandRepository.existsByName("Nike")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(savedBrand);

        BrandResponseDTO result = brandService.createBrand(createDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Nike");
        assertThat(result.getDescription()).isEqualTo("Just Do It");
        assertThat(result.getWebsite()).isEqualTo("https://www.nike.com");
        assertThat(result.getCountry()).isEqualTo("USA");
        assertThat(result.getActive()).isTrue();

        verify(brandRepository).existsByName("Nike");
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void createBrand_shouldThrowException_whenNameAlreadyExists() {
        BrandCreateDTO createDTO = new BrandCreateDTO();
        createDTO.setName("Nike");

        when(brandRepository.existsByName("Nike")).thenReturn(true);

        assertThatThrownBy(() -> brandService.createBrand(createDTO))
                .isInstanceOf(ValidationException.class);

        verify(brandRepository).existsByName("Nike");
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    void getActiveBrands_shouldReturnActiveBrands() {
        Brand brand1 = createMockBrand(1L, "Nike", true);
        Brand brand2 = createMockBrand(2L, "Adidas", true);

        when(brandRepository.findByActiveOrderByNameAsc(true))
                .thenReturn(Arrays.asList(brand1, brand2));

        List<BrandResponseDTO> result = brandService.getActiveBrands();

        assertThat(result).hasSize(2);
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Nike");
        assertThat(result.get(1).getName()).isEqualTo("Adidas");

        verify(brandRepository).findByActiveOrderByNameAsc(true);
    }

    @Test
    void getBrandById_shouldReturnBrand() {
        Brand brand = createMockBrand(1L, "Nike", true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));

        BrandResponseDTO result = brandService.getBrandById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Nike");

        verify(brandRepository).findById(1L);
    }

    @Test
    void getBrandById_shouldThrowException_whenNotFound() {
        when(brandRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> brandService.getBrandById(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(brandRepository).findById(999L);
    }

    @Test
    void updateBrand_shouldUpdateSuccessfully() {
        Brand existingBrand = createMockBrand(1L, "Nike", true);
        BrandUpdateDTO updateDTO = new BrandUpdateDTO();
        updateDTO.setName("Nike Updated");
        updateDTO.setDescription("New Description");

        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByName("Nike Updated")).thenReturn(false);
        when(brandRepository.save(any(Brand.class))).thenReturn(existingBrand);

        BrandResponseDTO result = brandService.updateBrand(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(brandRepository).findById(1L);
        verify(brandRepository).save(any(Brand.class));
    }

    @Test
    void updateBrand_shouldThrowException_whenNewNameAlreadyExists() {
        Brand existingBrand = createMockBrand(1L, "Nike", true);
        BrandUpdateDTO updateDTO = new BrandUpdateDTO();
        updateDTO.setName("Adidas");

        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.existsByName("Adidas")).thenReturn(true);

        assertThatThrownBy(() -> brandService.updateBrand(1L, updateDTO))
                .isInstanceOf(ValidationException.class);

        verify(brandRepository).findById(1L);
        verify(brandRepository, never()).save(any(Brand.class));
    }

    @Test
    void deleteBrand_shouldDeleteSuccessfully() {
        Brand brand = createMockBrand(1L, "Nike", true);

        when(brandRepository.findById(1L)).thenReturn(Optional.of(brand));
        doNothing().when(brandRepository).delete(brand);

        brandService.deleteBrand(1L);

        verify(brandRepository).findById(1L);
        verify(brandRepository).delete(brand);
    }

    @Test
    void getAllBrands_shouldReturnAllBrands() {
        Brand brand1 = createMockBrand(1L, "Nike", true);
        Brand brand2 = createMockBrand(2L, "Adidas", false);

        when(brandRepository.findAll()).thenReturn(Arrays.asList(brand1, brand2));

        List<BrandResponseDTO> result = brandService.getAllBrands();

        assertThat(result).hasSize(2);
        verify(brandRepository).findAll();
    }

    private Brand createMockBrand(Long id, String name, Boolean active) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        brand.setDescription("Description for " + name);
        brand.setActive(active);
        brand.setCreatedAt(LocalDateTime.now());
        brand.setUpdatedAt(LocalDateTime.now());
        return brand;
    }
}
