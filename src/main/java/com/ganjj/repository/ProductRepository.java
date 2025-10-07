package com.ganjj.repository;

import com.ganjj.entities.Product;
import com.ganjj.entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    Page<Product> findByActive(Boolean active, Pageable pageable);
    
    List<Product> findByFeaturedAndActive(Boolean featured, Boolean active);
    
    List<Product> findByCategoryAndActive(Category category, Boolean active);
    
    Page<Product> findByCategoryAndActive(Category category, Boolean active, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.active = :active AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:search IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findByFilters(
            @Param("active") Boolean active,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("categoryId") Long categoryId,
            @Param("search") String search,
            Pageable pageable);
}