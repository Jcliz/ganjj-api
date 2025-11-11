package com.ganjj.service;

import com.ganjj.dto.CategoryCreateDTO;
import com.ganjj.dto.CategoryResponseDTO;
import com.ganjj.dto.CategoryUpdateDTO;
import com.ganjj.entities.Category;
import com.ganjj.exception.ConflictException;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDTO createCategory(CategoryCreateDTO categoryCreateDTO) {
        if (categoryRepository.findByName(categoryCreateDTO.getName()).isPresent()) {
            throw new ValidationException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
        }

        Category category = new Category();
        category.setName(categoryCreateDTO.getName());
        category.setDescription(categoryCreateDTO.getDescription());
        
        Category savedCategory = categoryRepository.save(category);
        
        return new CategoryResponseDTO(savedCategory);
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getActiveCategories() {
        List<Category> categories = categoryRepository.findByActive(true);
        return categories.stream()
                .map(CategoryResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, id));
        
        return new CategoryResponseDTO(category);
    }
    
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO categoryUpdateDTO) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, id));
        
        if (categoryUpdateDTO.getName() != null && !categoryUpdateDTO.getName().equals(category.getName())) {
            if (categoryRepository.findByName(categoryUpdateDTO.getName()).isPresent()) {
                throw new ValidationException(ErrorCode.CATEGORY_NAME_ALREADY_EXISTS);
            }
            category.setName(categoryUpdateDTO.getName());
        }
        
        if (categoryUpdateDTO.getDescription() != null) {
            category.setDescription(categoryUpdateDTO.getDescription());
        }
        
        if (categoryUpdateDTO.getActive() != null) {
            category.setActive(categoryUpdateDTO.getActive());
        }
        
        Category savedCategory = categoryRepository.save(category);
        
        return new CategoryResponseDTO(savedCategory);
    }
    
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, id));
        
        if (!category.getProducts().isEmpty()) {
            throw new ConflictException(ErrorCode.CATEGORY_HAS_PRODUCTS);
        }
        
        categoryRepository.delete(category);
    }
}