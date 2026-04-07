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
        applyDefaultProductValues(product);
        product.setCreateTime(new Date());
        product.setUpdateTime(new Date());
        return productRepository.save(product);
    }

    public Page<Product> searchProducts(String keyword, String category, Pageable pageable) {
        String normalizedKeyword = keyword == null ? null : keyword.trim();
        String normalizedCategory = category == null ? null : category.trim();
        if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
            normalizedKeyword = null;
        }
        if (normalizedCategory != null && normalizedCategory.isEmpty()) {
            normalizedCategory = null;
        }
        return productRepository.searchMallProducts(normalizedKeyword, normalizedCategory, pageable);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Page<Product> getFarmerProducts(Long farmerId, Pageable pageable) {
        return productRepository.findByFarmerId(farmerId, pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Product updateProduct(Product product) {
        Product existingProduct = getProductById(product.getId());
        product.setCreateTime(existingProduct.getCreateTime());
        product.setUpdateTime(new Date());
        product.setDescription(existingProduct.getDescription());
        product.setImages(existingProduct.getImages());
        product.setIsOnSale(product.getIsOnSale() != null ? product.getIsOnSale() : existingProduct.getIsOnSale());
        product.setViewCount(product.getViewCount() != null ? product.getViewCount() : existingProduct.getViewCount());
        product.setSalesCount(product.getSalesCount() != null ? product.getSalesCount() : existingProduct.getSalesCount());
        applyDefaultProductValues(product);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new RuntimeException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private void applyDefaultProductValues(Product product) {
        if (product.getIsOnSale() == null) {
            product.setIsOnSale(true);
        }
        if (product.getViewCount() == null) {
            product.setViewCount(0);
        }
        if (product.getSalesCount() == null) {
            product.setSalesCount(0);
        }
    }

    // 其他产品相关方法...
}
 