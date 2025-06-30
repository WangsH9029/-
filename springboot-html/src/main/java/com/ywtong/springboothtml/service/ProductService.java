package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.Product;
import com.ywtong.springboothtml.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    
    public Product createProduct(Product product) {
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        return productRepository.save(product);
    }
    
    public Page<Product> searchProducts(String keyword, String category, Pageable pageable) {
        if (category != null && !category.isEmpty()) {
            return productRepository.findByCategory(category, pageable);
        }
        if (keyword != null && !keyword.isEmpty()) {
            return productRepository.findByNameContaining(keyword, pageable);
        }
        // 关键：无条件时查全部，用于与初始显示
        return productRepository.findAll(pageable);
    }
    
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }
    
    public Product updateProduct(Product product) {
        Product existingProduct = getProductById(product.getId());
        product.setCreateTime(existingProduct.getCreateTime());
        product.setUpdateTime(new Date());
        return productRepository.save(product);
    }
    
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }
    
    // 其他产品相关方法...
} 