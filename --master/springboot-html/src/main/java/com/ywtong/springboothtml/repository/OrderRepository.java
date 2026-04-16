package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByUserId(Long userId, Pageable pageable);
    Page<Order> findByStatus(String status, Pageable pageable);

    @Query("SELECT DISTINCT o FROM Order o JOIN OrderItem oi ON oi.order = o JOIN oi.product p WHERE p.farmer.id = ?1")
    Page<Order> findFarmerOrders(Long farmerId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.createTime >= :startDate")
    BigDecimal getTotalSalesAfter(@Param("startDate") Date startDate);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.createTime >= :startDate")
    Long countOrdersAfter(@Param("startDate") Date startDate);

    @Query("SELECT FUNCTION('DATE_FORMAT', o.createTime, '%Y-%m-%d') as date, COALESCE(SUM(o.totalAmount), 0) as amount, COUNT(o) as count FROM Order o WHERE o.createTime >= :startDate GROUP BY FUNCTION('DATE_FORMAT', o.createTime, '%Y-%m-%d') ORDER BY date")
    List<Object[]> getSalesByDateRange(@Param("startDate") Date startDate);

    @Query("SELECT oi.product.id, COALESCE(SUM(oi.quantity), 0) FROM OrderItem oi JOIN oi.order o WHERE o.createTime >= :startDate AND o.createTime < :endDate GROUP BY oi.product.id")
    List<Object[]> getProductSalesByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
}