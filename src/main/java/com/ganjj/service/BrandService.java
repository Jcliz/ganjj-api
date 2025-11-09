package com.ganjj.service;

import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.repository.BrandRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Transactional
    public BrandResponseDTO createBrand(BrandCreateDTO brandCreateDTO) {
        if (brandRepository.existsByName(brandCreateDTO.getName())) {
            throw new IllegalArgumentException("Já existe uma marca com este nome.");
        }

        Brand brand = new Brand();
        brand.setName(brandCreateDTO.getName());
        brand.setDescription(brandCreateDTO.getDescription());
        brand.setWebsite(brandCreateDTO.getWebsite());
        brand.setCountry(brandCreateDTO.getCountry());
        
        Brand savedBrand = brandRepository.save(brand);
        
        return new BrandResponseDTO(savedBrand);
    }
    
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getAllBrands() {
        List<Brand> brands = brandRepository.findAll();
        return brands.stream()
                .map(BrandResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<BrandResponseDTO> getActiveBrands() {
        List<Brand> brands = brandRepository.findByActiveOrderByNameAsc(true);
        return brands.stream()
                .map(BrandResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public BrandResponseDTO getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marca não encontrada com o ID: " + id));
        
        return new BrandResponseDTO(brand);
    }
    
    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandUpdateDTO brandUpdateDTO) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marca não encontrada com o ID: " + id));
        
        if (brandUpdateDTO.getName() != null && !brandUpdateDTO.getName().equals(brand.getName())) {
            if (brandRepository.existsByName(brandUpdateDTO.getName())) {
                throw new IllegalArgumentException("Já existe uma marca com este nome.");
            }
            brand.setName(brandUpdateDTO.getName());
        }
        
        if (brandUpdateDTO.getDescription() != null) {
            brand.setDescription(brandUpdateDTO.getDescription());
        }
        
        if (brandUpdateDTO.getWebsite() != null) {
            brand.setWebsite(brandUpdateDTO.getWebsite());
        }
        
        if (brandUpdateDTO.getCountry() != null) {
            brand.setCountry(brandUpdateDTO.getCountry());
        }
        
        if (brandUpdateDTO.getActive() != null) {
            brand.setActive(brandUpdateDTO.getActive());
        }
        
        Brand savedBrand = brandRepository.save(brand);
        
        return new BrandResponseDTO(savedBrand);
    }
    
    @Transactional
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Marca não encontrada com o ID: " + id));
        
        if (!brand.getProducts().isEmpty()) {
            throw new IllegalStateException("Não é possível excluir uma marca que possui produtos associados.");
        }
        
        brandRepository.delete(brand);
    }
}