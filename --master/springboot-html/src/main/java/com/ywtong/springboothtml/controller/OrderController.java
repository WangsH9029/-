package com.ywtong.springboothtml.controller;

import com.ywtong.springboothtml.entity.CreateOrderRequest;
import com.ywtong.springboothtml.entity.Order;
import com.ywtong.springboothtml.entity.Resp;
import com.ywtong.springboothtml.entity.UpdateOrderContactRequest;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.service.OrderService;
import com.ywtong.springboothtml.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public Page<Order> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime", "id"));
        String role = getCurrentRole(session);
        User currentUser = getCurrentUser(session);
        if (isAdmin(role)) {
            return orderService.getAllOrders(pageable);
        }
        if (isFarmer(role)) {
            return orderService.getFarmerOrders(currentUser.getId(), pageable);
        }
        return orderService.getUserOrders(currentUser.getId(), pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<Order> getUserOrders(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (!isAdmin(getCurrentRole(session)) && !currentUser.getId().equals(userId)) {
            throw new RuntimeException("无权限查看该用户订单");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createTime", "id"));
        return orderService.getUserOrders(userId, pageable);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id, HttpSession session) {
        Order order = orderService.getOrderById(id);
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        if (isAdmin(role)) {
            return order;
        }
        if (isFarmer(role)) {
            if (!orderService.farmerCanAccessOrder(id, currentUser.getId())) {
                throw new RuntimeException("无权限查看该订单");
            }
            return order;
        }
        if (order.getUser() == null || !currentUser.getId().equals(order.getUser().getId())) {
            throw new RuntimeException("无权限查看该订单");
        }
        return order;
    }

    @PostMapping
    public Resp<Order> createOrder(@RequestBody CreateOrderRequest request, HttpSession session) {
        try {
            User currentUser = getCurrentUser(session);
            Order created = orderService.createOrder(request, currentUser.getId());
            return Resp.success(created);
        } catch (Exception e) {
            return Resp.fail("500", "创建订单失败: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/status")
    public Order updateOrderStatus(@PathVariable Long id, @RequestParam String status, HttpSession session) {
        User currentUser = getCurrentUser(session);
        String role = getCurrentRole(session);
        if (isAdmin(role)) {
            return orderService.updateOrderStatus(id, status);
        }
        if (isFarmer(role)) {
            return orderService.farmerUpdateOrderStatus(id, currentUser.getId(), status);
        }
        throw new RuntimeException("无权限修改订单状态");
    }

    @DeleteMapping("/{id}")
    public void deleteOrder(@PathVariable Long id, HttpSession session) {
        if (!isAdmin(getCurrentRole(session))) {
            throw new RuntimeException("无权限删除订单");
        }
        orderService.deleteOrder(id);
    }

    @DeleteMapping("/batch")
    public void batchDeleteOrders(@RequestBody List<Long> ids, HttpSession session) {
        if (!isAdmin(getCurrentRole(session))) {
            throw new RuntimeException("无权限批量删除订单");
        }
        orderService.batchDeleteOrders(ids);
    }

    @PutMapping("/{id}/pay")
    public Order payOrder(@PathVariable Long id, HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (isAdmin(getCurrentRole(session)) || isFarmer(getCurrentRole(session))) {
            throw new RuntimeException("仅普通用户可支付订单");
        }
        return orderService.payOrder(id, currentUser.getId());
    }

    @PutMapping("/{id}/contact")
    public Order updateOrderContact(@PathVariable Long id,
                                    @RequestBody UpdateOrderContactRequest request,
                                    HttpSession session) {
        User currentUser = getCurrentUser(session);
        if (isAdmin(getCurrentRole(session)) || isFarmer(getCurrentRole(session))) {
            throw new RuntimeException("仅普通用户可修改订单收货信息");
        }
        return orderService.updateOrderContact(id, currentUser.getId(), request);
    }

    @PutMapping("/{id}/cancel")
    public void cancelOrder(@PathVariable Long id, HttpSession session) {
        Order order = orderService.getOrderById(id);
        User currentUser = getCurrentUser(session);
        if (!isAdmin(getCurrentRole(session)) && (order.getUser() == null || !currentUser.getId().equals(order.getUser().getId()))) {
            throw new RuntimeException("无权限取消该订单");
        }
        orderService.cancelOrder(id);
    }

    @GetMapping("/statistics/status")
    public java.util.Map<String, Long> getOrderStatusStatistics(HttpSession session) {
        String role = getCurrentRole(session);
        User currentUser = getCurrentUser(session);
        if (isFarmer(role)) {
            return orderService.getOrderStatusStatistics(currentUser.getId());
        }
        if (isAdmin(role)) {
            return orderService.getOrderStatusStatistics(null);
        }
        throw new RuntimeException("无权限查看订单统计");
    }

    @GetMapping("/export")
    public void exportOrders(HttpServletResponse response, HttpSession session) throws IOException {
        if (!isAdmin(getCurrentRole(session))) {
            throw new RuntimeException("无权限导出订单");
        }
        orderService.exportOrdersToCSV(response);
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
