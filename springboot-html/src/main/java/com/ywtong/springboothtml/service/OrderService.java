package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.Order;
import com.ywtong.springboothtml.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Transactional
    public Order createOrder(Order order) {
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());
        order.setStatus("PENDING_PAYMENT");
        return orderRepository.save(order);
    }
    
    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }
    
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }
    
    @Transactional
    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        order.setUpdateTime(new Date());
        return orderRepository.save(order);
    }
    
    @Transactional
    public void cancelOrder(Long id) {
        Order order = getOrderById(id);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new RuntimeException("Only pending orders can be cancelled");
        }
        order.setStatus("CANCELLED");
        order.setUpdateTime(new Date());
        orderRepository.save(order);
    }
    
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }
    
    @Transactional
    public void deleteOrder(Long id) {
        orderRepository.deleteById(id);
    }

    // 其他订单相关方法...
} 