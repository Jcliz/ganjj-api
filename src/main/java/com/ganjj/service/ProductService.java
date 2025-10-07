package com.ganjj.service;

import com.ganjj.dto.ProductCreateDTO;
import com.ganjj.dto.ProductResponseDTO;
import com.ganjj.dto.ProductUpdateDTO;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import com.ganjj.repository.CategoryRepository;
import com.ganjj.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;
    
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO productCreateDTO) {
        Product product = new Product();
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setPrice(productCreateDTO.getPrice());
        product.setStockQuantity(productCreateDTO.getStockQuantity());
        product.setBrand(productCreateDTO.getBrand());
        
        if (productCreateDTO.getImageUrls() != null) {
            product.setImageUrls(productCreateDTO.getImageUrls());
        }
        
        if (productCreateDTO.getAvailableSizes() != null) {
            product.setAvailableSizes(productCreateDTO.getAvailableSizes());
        }
        
        if (productCreateDTO.getAvailableColors() != null) {
            product.setAvailableColors(productCreateDTO.getAvailableColors());
        }
        
        if (productCreateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + productCreateDTO.getCategoryId()));
            product.setCategory(category);
        }
        
        product.setMaterial(productCreateDTO.getMaterial());
        product.setCareInstructions(productCreateDTO.getCareInstructions());
        
        if (productCreateDTO.getFeatured() != null) {
            product.setFeatured(productCreateDTO.getFeatured());
        }
        
        if (productCreateDTO.getDiscountPercent() != null) {
            product.setDiscountPercent(productCreateDTO.getDiscountPercent());
        }
        
        Product savedProduct = productRepository.save(product);
        
        return new ProductResponseDTO(savedProduct);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getAllProducts(Pageable pageable) {
        Page<Product> products = productRepository.findAll(pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByActive(true, pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));
        
        return new ProductResponseDTO(product);
    }
    
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getFeaturedProducts() {
        List<Product> products = productRepository.findByFeaturedAndActive(true, true);
        return products.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(
            BigDecimal minPrice, BigDecimal maxPrice, 
            Long categoryId, String search, Pageable pageable) {
        
        Page<Product> products = productRepository.findByFilters(true, minPrice, maxPrice, categoryId, search, pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + categoryId));
        
        Page<Product> products = productRepository.findByCategoryAndActive(category, true, pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com o ID: " + id));
        
        if (productUpdateDTO.getName() != null) {
            product.setName(productUpdateDTO.getName());
        }
        
        if (productUpdateDTO.getDescription() != null) {
            product.setDescription(productUpdateDTO.getDescription());
        }
        
        if (productUpdateDTO.getPrice() != null) {
            product.setPrice(productUpdateDTO.getPrice());
        }
        
        if (productUpdateDTO.getStockQuantity() != null) {
            product.setStockQuantity(productUpdateDTO.getStockQuantity());
        }
        
        if (productUpdateDTO.getBrand() != null) {
            product.setBrand(productUpdateDTO.getBrand());
        }
        
        if (productUpdateDTO.getImageUrls() != null) {
            product.setImageUrls(productUpdateDTO.getImageUrls());
        }
        
        if (productUpdateDTO.getAvailableSizes() != null) {
            product.setAvailableSizes(productUpdateDTO.getAvailableSizes());
        }
        
        if (productUpdateDTO.getAvailableColors() != null) {
            product.setAvailableColors(productUpdateDTO.getAvailableColors());
        }
        
        if (productUpdateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productUpdateDTO.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com o ID: " + productUpdateDTO.getCategoryId()));
            product.setCategory(category);
        }
        
        if (productUpdateDTO.getMaterial() != null) {
            product.setMaterial(productUpdateDTO.getMaterial());
        }
        
        if (productUpdateDTO.getCareInstructions() != null) {
            product.setCareInstructions(productUpdateDTO.getCareInstructions());
        }
        
        if (productUpdateDTO.getActive() != null) {
            product.setActive(productUpdateDTO.getActive());
        }
        
        if (productUpdateDTO.getFeatured() != null) {
            product.setFeatured(productUpdateDTO.getFeatured());
        }
        
        if (productUpdateDTO.getDiscountPercent() != null) {
            product.setDiscountPercent(productUpdateDTO.getDiscountPercent());
        }
        
        Product savedProduct = productRepository.save(product);
        
        return new ProductResponseDTO(savedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Produto não encontrado com o ID: " + id);
        }
        
        productRepository.deleteById(id);
    }
}