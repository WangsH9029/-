package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @Query("SELECT p FROM Product p WHERE p.isOnSale = true AND p.stock > 0 AND (:keyword IS NULL OR :keyword = '' OR p.name LIKE %:keyword%) AND (:category IS NULL OR :category = '' OR p.category = :category)")
    Page<Product> searchMallProducts(@Param("keyword") String keyword,
                                     @Param("category") String category,
                                     Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.farmer.id = ?1")
    Page<Product> findByFarmerId(Long farmerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.salesCount IS NOT NULL ORDER BY p.salesCount DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);

    @Query("SELECT p.category, COUNT(p) FROM Product p WHERE p.category IS NOT NULL GROUP BY p.category")
    List<Object[]> getProductCountByCategory();
}
 