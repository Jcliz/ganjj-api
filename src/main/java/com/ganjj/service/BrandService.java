package com.ganjj.service;

import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.exception.ConflictException;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.BrandRepository;
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
            throw new ValidationException(ErrorCode.BRAND_NAME_ALREADY_EXISTS);
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND, id));
        
        return new BrandResponseDTO(brand);
    }
    
    @Transactional
    public BrandResponseDTO updateBrand(Long id, BrandUpdateDTO brandUpdateDTO) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND, id));
        
        if (brandUpdateDTO.getName() != null && !brandUpdateDTO.getName().equals(brand.getName())) {
            if (brandRepository.existsByName(brandUpdateDTO.getName())) {
                throw new ValidationException(ErrorCode.BRAND_NAME_ALREADY_EXISTS);
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
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND, id));
        
        if (!brand.getProducts().isEmpty()) {
            throw new ConflictException(ErrorCode.BRAND_HAS_PRODUCTS);
        }
        
        brandRepository.delete(brand);
    }
}