package com.ganjj.repository;

import com.ganjj.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByParentIsNull();
    
    List<Category> findByParentIsNullAndActive(Boolean active);
    
    List<Category> findByActive(Boolean active);
}