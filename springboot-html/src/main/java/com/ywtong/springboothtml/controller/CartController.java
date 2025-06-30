package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestParam Long userId,
            @RequestParam Long productId,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.addToCart(userId, productId, quantity));
    }
    
    @GetMapping("/items")
    public ResponseEntity<?> getCartItems(@RequestParam Long userId) {
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }
    
    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeFromCart(@PathVariable Long id) {
        cartService.removeFromCart(id);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/items/{id}")
    public ResponseEntity<?> updateQuantity(
            @PathVariable Long id,
            @RequestParam Integer quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity));
    }
} 