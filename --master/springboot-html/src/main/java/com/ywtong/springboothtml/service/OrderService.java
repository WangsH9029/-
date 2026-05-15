package com.ywtong.springboothtml.service;

import com.ywtong.springboothtml.entity.CreateOrderItemRequest;
import com.ywtong.springboothtml.entity.CreateOrderRequest;
import com.ywtong.springboothtml.entity.Order;
import com.ywtong.springboothtml.entity.OrderItem;
import com.ywtong.springboothtml.entity.Product;
import com.ywtong.springboothtml.entity.UpdateOrderContactRequest;
import com.ywtong.springboothtml.entity.User;
import com.ywtong.springboothtml.repository.OrderItemRepository;
import com.ywtong.springboothtml.repository.OrderRepository;
import com.ywtong.springboothtml.repository.ProductRepository;
import com.ywtong.springboothtml.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest request, Long userId) {
        if (userId == null) {
            throw new RuntimeException("当前登录用户不能为空");
        }
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("订单商品不能为空");
        }
        if (request.getReceiverName() == null || request.getReceiverName().trim().isEmpty()) {
            throw new RuntimeException("收货人不能为空");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new RuntimeException("地址不能为空");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("联系电话不能为空");
        }

        String phone = request.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确，必须为11位数字，当前值: " + phone);
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("用户不存在"));

        Order order = new Order();
        order.setUser(user);
        order.setOrderNo(generateOrderNo());
        order.setStatus("PENDING_PAYMENT");
        order.setAddress(request.getAddress().trim());
        order.setPhone(phone);
        order.setReceiverName(request.getReceiverName().trim());
        order.setPaymentMethod(request.getPaymentMethod());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            if (itemRequest.getProductId() == null) {
                throw new RuntimeException("商品不能为空");
            }
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new RuntimeException("商品数量必须大于0");
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));
            if (!Boolean.TRUE.equals(product.getIsOnSale())) {
                throw new RuntimeException("商品未上架，无法下单");
            }
            if (product.getStock() == null || product.getStock() < itemRequest.getQuantity()) {
                throw new RuntimeException("商品库存不足: " + product.getName());
            }
            if (product.getPrice() == null) {
                throw new RuntimeException("商品价格不能为空: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
            totalAmount = totalAmount.add(subtotal);
        }

        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);

        for (CreateOrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                .orElseThrow(() -> new RuntimeException("商品不存在"));

            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(savedOrder);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(product.getPrice());
            orderItem.setSubtotal(subtotal);
            orderItem.setTotalPrice(subtotal);
            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - itemRequest.getQuantity());
            Integer salesCount = product.getSalesCount() == null ? 0 : product.getSalesCount();
            product.setSalesCount(salesCount + itemRequest.getQuantity());
            productRepository.save(product);
        }

        return savedOrder;
    }

    public Page<Order> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserId(userId, pageable);
    }

    public Page<Order> getFarmerOrders(Long farmerId, Pageable pageable) {
        return orderRepository.findFarmerOrders(farmerId, pageable);
    }

    public boolean farmerCanAccessOrder(Long orderId, Long farmerId) {
        return orderItemRepository.existsByOrderIdAndProductFarmerId(orderId, farmerId);
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
    public Order farmerUpdateOrderStatus(Long id, Long farmerId, String status) {
        if (!farmerCanAccessOrder(id, farmerId)) {
            throw new RuntimeException("无权限处理该订单");
        }
        Order order = getOrderById(id);
        if ("SHIPPED".equals(status)) {
            if (!"PAID".equals(order.getStatus())) {
                throw new RuntimeException("只有已支付订单才能发货");
            }
        } else if ("COMPLETED".equals(status)) {
            if (!"SHIPPED".equals(order.getStatus())) {
                throw new RuntimeException("只有已发货订单才能完成");
            }
        } else {
            throw new RuntimeException("农户只能执行发货或完成操作");
        }
        order.setStatus(status);
        order.setUpdateTime(new Date());
        return orderRepository.save(order);
    }

    @Transactional
    public Order payOrder(Long id, Long userId) {
        Order order = getOrderById(id);
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限支付该订单");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new RuntimeException("当前订单不可支付");
        }
        order.setStatus("PAID");
        order.setUpdateTime(new Date());
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateOrderContact(Long id, Long userId, UpdateOrderContactRequest request) {
        Order order = getOrderById(id);
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new RuntimeException("无权限修改该订单收货信息");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new RuntimeException("只有待支付订单才能修改收货信息");
        }
        if (request == null) {
            throw new RuntimeException("收货信息不能为空");
        }
        if (request.getAddress() == null || request.getAddress().trim().isEmpty()) {
            throw new RuntimeException("地址不能为空");
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            throw new RuntimeException("联系电话不能为空");
        }

        String phone = request.getPhone().trim();
        if (!phone.matches("^1[3-9]\\d{9}$")) {
            throw new RuntimeException("手机号码格式不正确，必须为11位数字，当前值: " + phone);
        }

        order.setAddress(request.getAddress().trim());
        order.setPhone(phone);
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
        // 先删除订单明细
        orderItemRepository.deleteByOrderId(id);
        // 再删除订单
        orderRepository.deleteById(id);
    }

    @Transactional
    public void batchDeleteOrders(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new RuntimeException("订单ID列表不能为空");
        }
        // 先删除所有订单的明细
        for (Long id : ids) {
            orderItemRepository.deleteByOrderId(id);
        }
        // 再批量删除订单
        orderRepository.deleteAllById(ids);
    }

    public void exportOrdersToCSV(javax.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=orders_" +
            new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".csv");

        List<Order> orders = orderRepository.findAll();

        java.io.PrintWriter writer = response.getWriter();

        // 添加UTF-8 BOM头,让Excel正确识别编码
        writer.write('\uFEFF');

        // CSV表头
        writer.println("订单编号,用户名,订单金额,订单状态,收货人,联系电话,收货地址,支付方式,创建时间");

        // CSV数据行
        for (Order order : orders) {
            String username = order.getUser() != null ? order.getUser().getUsername() : "";
            String statusName = getStatusName(order.getStatus());
            String createTime = order.getCreateTime() != null ?
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getCreateTime()) : "";

            writer.println(String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s",
                order.getOrderNo(),
                username,
                order.getTotalAmount(),
                statusName,
                order.getReceiverName(),
                order.getPhone(),
                order.getAddress(),
                order.getPaymentMethod(),
                createTime
            ));
        }
        writer.flush();
    }

    private String getStatusName(String status) {
        switch(status) {
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "PAID": return "已支付";
            case "SHIPPED": return "已发货";
            case "PENDING_PAYMENT": return "待支付";
            default: return status;
        }
    }

    private String generateOrderNo() {
        return "ORD" + new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
    }

    // 获取订单状态统计
    public java.util.Map<String, Long> getOrderStatusStatistics(Long farmerId) {
        java.util.Map<String, Long> statistics = new java.util.HashMap<>();
        if (farmerId != null) {
            // 农户统计
            statistics.put("PENDING_PAYMENT", orderRepository.countFarmerOrdersByStatus(farmerId, "PENDING_PAYMENT"));
            statistics.put("PAID", orderRepository.countFarmerOrdersByStatus(farmerId, "PAID"));
            statistics.put("SHIPPED", orderRepository.countFarmerOrdersByStatus(farmerId, "SHIPPED"));
            statistics.put("COMPLETED", orderRepository.countFarmerOrdersByStatus(farmerId, "COMPLETED"));
            statistics.put("CANCELLED", orderRepository.countFarmerOrdersByStatus(farmerId, "CANCELLED"));
        } else {
            // 全局统计
            statistics.put("PENDING_PAYMENT", orderRepository.countByStatus("PENDING_PAYMENT"));
            statistics.put("PAID", orderRepository.countByStatus("PAID"));
            statistics.put("SHIPPED", orderRepository.countByStatus("SHIPPED"));
            statistics.put("COMPLETED", orderRepository.countByStatus("COMPLETED"));
            statistics.put("CANCELLED", orderRepository.countByStatus("CANCELLED"));
        }
        return statistics;
    }
}
