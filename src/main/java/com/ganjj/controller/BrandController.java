package com.ganjj.controller;

import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.service.BrandService;
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
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping
    public ResponseEntity<BrandResponseDTO> createBrand(@Valid @RequestBody BrandCreateDTO brandCreateDTO) {
        BrandResponseDTO createdBrand = brandService.createBrand(brandCreateDTO);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdBrand.getId()).toUri();
        
        return ResponseEntity.created(uri).body(createdBrand);
    }
    
    @GetMapping("/admin/all")
    public ResponseEntity<List<BrandResponseDTO>> getAllBrands() {
        return ResponseEntity.ok(brandService.getAllBrands());
    }
    
    @GetMapping
    public ResponseEntity<List<BrandResponseDTO>> getActiveBrands() {
        return ResponseEntity.ok(brandService.getActiveBrands());
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable Long id) {
        return ResponseEntity.ok(brandService.getBrandById(id));
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandUpdateDTO brandUpdateDTO) {
        
        return ResponseEntity.ok(brandService.updateBrand(id, brandUpdateDTO));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
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