package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.CartItem;
import com.ywtong.springboothtml.entity.Product;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.repository.CartItemRepository;
import com.ywtong.springboothtml.repository.ProductRepository;
import com.ywtong.springboothtml.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.List;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    public CartItem addToCart(Long userId, Long productId, Integer quantity) {
        // 验证用户和商品是否存在
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 检查库存
        if (product.getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        CartItem cartItem = new CartItem();
        cartItem.setUser(user);
        cartItem.setProduct(product);
        cartItem.setQuantity(quantity);
        cartItem.setCreateTime(new Date());
        cartItem.setUpdateTime(new Date());
        
        return cartItemRepository.save(cartItem);
    }
    
    public List<CartItem> getCartItems(Long userId) {
        return cartItemRepository.findByUserId(userId);
    }
    
    public void deleteCartItem(Long id) {
        cartItemRepository.deleteById(id);
    }
    
    public CartItem updateCartItem(Long id, Integer quantity) {
        CartItem cartItem = cartItemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("购物车项不存在"));
        
        // 检查库存
        if (cartItem.getProduct().getStock() < quantity) {
            throw new RuntimeException("商品库存不足");
        }
        
        cartItem.setQuantity(quantity);
        cartItem.setUpdateTime(new Date());
        return cartItemRepository.save(cartItem);
    }
    
    public void removeFromCart(Long id) {
        deleteCartItem(id);
    }
    
    public CartItem updateQuantity(Long id, Integer quantity) {
        return updateCartItem(id, quantity);
    }
} 