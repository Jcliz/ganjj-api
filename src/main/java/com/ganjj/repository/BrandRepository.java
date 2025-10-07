package com.ganjj.repository;

import com.ganjj.entities.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    Optional<Brand> findByName(String name);
    
    List<Brand> findByActive(Boolean active);
    
    List<Brand> findByActiveOrderByNameAsc(Boolean active);
    
    boolean existsByName(String name);
}