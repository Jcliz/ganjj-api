package com.ganjj.service;

import com.ganjj.dto.ProductCreateDTO;
import com.ganjj.dto.ProductResponseDTO;
import com.ganjj.dto.ProductUpdateDTO;
import com.ganjj.entities.Brand;
import com.ganjj.entities.Category;
import com.ganjj.entities.Product;
import com.ganjj.exception.ErrorCode;
import com.ganjj.exception.ResourceNotFoundException;
import com.ganjj.exception.ValidationException;
import com.ganjj.repository.BrandRepository;
import com.ganjj.repository.CategoryRepository;
import com.ganjj.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Transactional
    public ProductResponseDTO createProduct(ProductCreateDTO productCreateDTO) {
        if (productCreateDTO.getPrice() != null && productCreateDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException(ErrorCode.PRODUCT_PRICE_INVALID);
        }

        if (productCreateDTO.getStockQuantity() != null && productCreateDTO.getStockQuantity() < 0) {
            throw new ValidationException(ErrorCode.PRODUCT_STOCK_NEGATIVE);
        }

        Product product = new Product();
        product.setName(productCreateDTO.getName());
        product.setDescription(productCreateDTO.getDescription());
        product.setPrice(productCreateDTO.getPrice());
        product.setStockQuantity(productCreateDTO.getStockQuantity());
        
        if (productCreateDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(productCreateDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND, productCreateDTO.getBrandId()));
            product.setBrand(brand);
        }
        
        if (productCreateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productCreateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, productCreateDTO.getCategoryId()));
            product.setCategory(category);
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
    public List<ProductResponseDTO> getAllProductsList() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductResponseDTO::new)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> getActiveProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByActive(true, pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional(readOnly = true)
    public ProductResponseDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, id));
        
        return new ProductResponseDTO(product);
    }
    
    @Transactional(readOnly = true)
    public Page<ProductResponseDTO> searchProducts(
            BigDecimal minPrice, BigDecimal maxPrice, 
            Long categoryId, Long brandId, String search, Pageable pageable) {
        
        Page<Product> products = productRepository.findByFilters(true, minPrice, maxPrice, categoryId, brandId, search, pageable);
        return products.map(ProductResponseDTO::new);
    }
    
    @Transactional
    public ProductResponseDTO updateProduct(Long id, ProductUpdateDTO productUpdateDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, id));
        
        if (productUpdateDTO.getName() != null) {
            product.setName(productUpdateDTO.getName());
        }
        
        if (productUpdateDTO.getDescription() != null) {
            product.setDescription(productUpdateDTO.getDescription());
        }
        
        if (productUpdateDTO.getPrice() != null) {
            if (productUpdateDTO.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ValidationException(ErrorCode.PRODUCT_PRICE_INVALID);
            }
            product.setPrice(productUpdateDTO.getPrice());
        }
        
        if (productUpdateDTO.getStockQuantity() != null) {
            if (productUpdateDTO.getStockQuantity() < 0) {
                throw new ValidationException(ErrorCode.PRODUCT_STOCK_NEGATIVE);
            }
            product.setStockQuantity(productUpdateDTO.getStockQuantity());
        }
        
        if (productUpdateDTO.getBrandId() != null) {
            Brand brand = brandRepository.findById(productUpdateDTO.getBrandId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.BRAND_NOT_FOUND, productUpdateDTO.getBrandId()));
            product.setBrand(brand);
        }
        
        if (productUpdateDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(productUpdateDTO.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.CATEGORY_NOT_FOUND, productUpdateDTO.getCategoryId()));
            product.setCategory(category);
        }
        
        if (productUpdateDTO.getActive() != null) {
            product.setActive(productUpdateDTO.getActive());
        }
        
        Product savedProduct = productRepository.save(product);
        
        return new ProductResponseDTO(savedProduct);
    }
    
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException(ErrorCode.PRODUCT_NOT_FOUND, id);
        }
        
        productRepository.deleteById(id);
    }
}