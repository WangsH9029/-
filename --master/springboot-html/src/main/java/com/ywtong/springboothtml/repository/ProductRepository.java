package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategory(String category, Pageable pageable);
    Page<Product> findByNameContaining(String keyword, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.farmer.id = ?1")
    Page<Product> findByFarmerId(Long farmerId, Pageable pageable);
} 