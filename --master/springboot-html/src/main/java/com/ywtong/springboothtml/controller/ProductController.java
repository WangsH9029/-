package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.Product;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.service.ProductService;
import com.ywtong.springboothtml.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private ProductService productService;

    @Autowired
    private UserService userService;

    @GetMapping("/search")
    public Page<Product> searchProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 构建排序对象
        Sort sort = Sort.unsorted();
        if (sortBy != null && !sortBy.isEmpty()) {
            Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = Sort.by(direction, sortBy);
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        return productService.searchProducts(keyword, category, minPrice, maxPrice, pageable);
    }

    @GetMapping("/{id}")
    public Product getProduct(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/my")
    public Page<Product> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        Pageable pageable = PageRequest.of(page, size);
        if (isFarmer(role)) {
            return productService.getFarmerProducts(currentUser.getId(), pageable);
        }
        if (isAdmin(role)) {
            return productService.getAllProducts(pageable);
        }
        throw new RuntimeException("无权限查看商品管理列表");
    }

    @PostMapping
    public Product createProduct(@RequestBody Product product, HttpSession session) {
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        if (!isAdmin(role) && !isFarmer(role)) {
            throw new RuntimeException("无权限创建商品");
        }
        if (isFarmer(role)) {
            product.setFarmer(currentUser);
        } else if (product.getFarmer() != null && product.getFarmer().getId() != null) {
            product.setFarmer(userService.getUserById(product.getFarmer().getId()));
        }
        return productService.createProduct(product);
    }

    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable Long id, @RequestBody Product product, HttpSession session) {
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        Product existing = productService.getProductById(id);
        if (isFarmer(role) && (existing.getFarmer() == null || !currentUser.getId().equals(existing.getFarmer().getId()))) {
            throw new RuntimeException("无权限修改该商品");
        }
        if (!isAdmin(role) && !isFarmer(role)) {
            throw new RuntimeException("无权限修改商品");
        }
        product.setId(id);
        if (isFarmer(role)) {
            product.setFarmer(currentUser);
        } else if (product.getFarmer() != null && product.getFarmer().getId() != null) {
            product.setFarmer(userService.getUserById(product.getFarmer().getId()));
        } else {
            product.setFarmer(existing.getFarmer());
        }
        return productService.updateProduct(product);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        Product existing = productService.getProductById(id);
        if (isFarmer(role) && (existing.getFarmer() == null || !currentUser.getId().equals(existing.getFarmer().getId()))) {
            throw new RuntimeException("无权限删除该商品");
        }
        if (!isAdmin(role) && !isFarmer(role)) {
            throw new RuntimeException("无权限删除商品");
        }
        productService.deleteProduct(id);
    }

    private User getCurrentUser(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return userService.getUserById(Long.valueOf(userId.toString()));
    }

    private String getCurrentRole(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        return role == null ? null : role.toString();
    }

    private boolean isAdmin(String role) {
        return "ROLE_ADMIN".equals(role);
    }

    private boolean isFarmer(String role) {
        return "ROLE_FARMER".equals(role);
    }
}
