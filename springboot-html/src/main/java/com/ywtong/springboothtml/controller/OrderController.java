package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.Order;
import com.ywtong.springboothtml.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order) {
        return ResponseEntity.ok(orderService.createOrder(order));
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<Order>> getUserOrders(
            @PathVariable Long userId,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.getUserOrders(userId, pageable));
    }
    
    // 其他订单相关接口...
} 