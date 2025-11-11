package com.ganjj.controller;

import com.ganjj.dto.BrandCreateDTO;
import com.ganjj.dto.BrandResponseDTO;
import com.ganjj.dto.BrandUpdateDTO;
import com.ganjj.service.BrandService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponseDTO> createBrand(@Valid @RequestBody BrandCreateDTO brandCreateDTO) {
        BrandResponseDTO createdBrand = brandService.createBrand(brandCreateDTO);
        
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(createdBrand.getId()).toUri();
        
        return ResponseEntity.created(uri).body(createdBrand);
    }
    
    @GetMapping
    public ResponseEntity<List<BrandResponseDTO>> getActiveBrands() {
        return ResponseEntity.ok(brandService.getActiveBrands());
    }
    
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long id,
            @Valid @RequestBody BrandUpdateDTO brandUpdateDTO) {
        
        return ResponseEntity.ok(brandService.updateBrand(id, brandUpdateDTO));
    }
    
    @DeleteMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.noContent().build();
    }
}