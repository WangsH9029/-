package com.ywtong.springboothtml.repository;

import com.ywtong.springboothtml.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrderIdAndProductFarmerId(Long orderId, Long farmerId);
    boolean existsByOrderIdAndProductFarmerId(Long orderId, Long farmerId);
    void deleteByOrderId(Long orderId);
}
