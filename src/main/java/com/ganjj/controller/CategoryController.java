package com.ganjj.controller;

import com.ganjj.dto.CategoryCreateDTO;
import com.ganjj.dto.CategoryResponseDTO;
import com.ganjj.dto.CategoryUpdateDTO;
import com.ganjj.service.CategoryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @PostMapping("/admin")
    public ResponseEntity<CategoryResponseDTO> createCategory(@Valid @RequestBody CategoryCreateDTO categoryCreateDTO) {
        CategoryResponseDTO createdCategory = categoryService.createCategory(categoryCreateDTO);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdCategory.getId()).toUri();
        
        return ResponseEntity.created(uri).body(createdCategory);
    }
    
    @PutMapping("/admin/{id}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO categoryUpdateDTO) {
        
        return ResponseEntity.ok(categoryService.updateCategory(id, categoryUpdateDTO));
    }
    
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    }
    
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
    
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalStateException(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}