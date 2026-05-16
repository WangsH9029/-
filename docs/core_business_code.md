# 核心业务功能关键代码

本文档整理了农产品交易系统三个核心业务功能的关键代码,供论文使用。

---

## 一、商城商品浏览与搜索功能

### 1.1 功能描述
用户登录后通过侧边栏商城按钮跳转商城面,商城通过分页功能从数据库调取所有农户上传的在售商品供用户浏览。同时商城提供按分类查找、按关键词模糊查找两种查找条件供用户搜索。

### 1.2 后端接口实现

**文件位置**: `ProductController.java:28-36`

```java
@GetMapping("/search")
public Page<Product> searchProducts(
        @RequestParam(required = false) String keyword,
        @RequestParam(required = false) String category,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    return productService.searchProducts(keyword, category, pageable);
}
```

**文件位置**: `ProductService.java:24-34`

```java
public Page<Product> searchProducts(String keyword, String category, Pageable pageable) {
    // 标准化搜索参数,去除空白字符
    String normalizedKeyword = keyword == null ? null : keyword.trim();
    String normalizedCategory = category == null ? null : category.trim();
    
    // 将空字符串转换为null,避免无效查询
    if (normalizedKeyword != null && normalizedKeyword.isEmpty()) {
        normalizedKeyword = null;
    }
    if (normalizedCategory != null && normalizedCategory.isEmpty()) {
        normalizedCategory = null;
    }
    
    // 调用Repository层执行分页查询
    return productRepository.searchMallProducts(normalizedKeyword, normalizedCategory, pageable);
}
```

### 1.3 前端实现

**文件位置**: `mall.html:41-47` (搜索栏UI)

```html
<div class="search-bar">
    <el-input v-model="keyword" placeholder="请输入商品名称关键词" 
              clearable style="width: 260px;" 
              @input="debouncedSearch" 
              @keyup.enter.native="searchProducts">
    </el-input>
    <el-select v-model="category" placeholder="请选择分类" 
               clearable style="width: 180px;" 
               @change="searchProducts">
        <el-option v-for="item in categoryOptions" 
                   :key="item" :label="item" :value="item">
        </el-option>
    </el-select>
    <el-button type="primary" @click="searchProducts">搜索</el-button>
</div>
```

**文件位置**: `mall.html:217-237` (搜索逻辑)

```javascript
methods: {
    fetchProducts() {
        // 发送GET请求到后端搜索接口
        $axios.get('/demo/api/product/search', {
            params: {
                keyword: this.keyword || undefined,
                category: this.category || undefined,
                page: this.page,
                size: this.size
            }
        }).then(res => {
            // 解析响应数据
            this.products = res.data.content || [];
            this.total = res.data.totalElements || this.products.length;
            
            // 初始化每个商品的购买数量
            this.products.forEach(item => {
                if (!this.quantities[item.id]) {
                    this.$set(this.quantities, item.id, 1);
                }
            });
        });
    },
    searchProducts() {
        this.page = 0;  // 重置到第一页
        this.fetchProducts();
    }
}
```

**文件位置**: `mall.html:70-79` (分页组件)

```html
<div class="pager">
    <el-pagination
        background
        layout="prev, pager, next, total"
        :current-page="page + 1"
        :page-size="size"
        :total="total"
        @current-change="handlePageChange">
    </el-pagination>
</div>
```

### 1.4 核心流程说明

1. **用户输入搜索条件**: 用户在搜索栏输入关键词或选择分类
2. **防抖优化**: 输入框使用500ms防抖,减少不必要的API请求
3. **发送搜索请求**: 前端通过axios发送GET请求到`/api/product/search`
4. **后端处理**: 
   - Controller接收参数(keyword, category, page, size)
   - Service层标准化参数并调用Repository
   - Repository执行数据库查询,返回分页结果
5. **前端渲染**: 接收分页数据,渲染商品列表和分页组件

---

## 二、用户下单功能

### 2.1 功能描述
用户选择数量后点击"立即下单"键进入确认订单界面,填写收货人、联系电话、收货地址、支付方式后可选提交订单。

### 2.2 前端订单确认界面

**文件位置**: `mall.html:82-116` (订单确认弹窗)

```html
<el-dialog title="确认订单" :visible.sync="dialogVisible" width="520px">
    <el-form :model="orderForm" :rules="rules" ref="orderForm" label-width="90px">
        <el-form-item label="商品名称">
            <span>{{ selectedProduct.name }}</span>
        </el-form-item>
        <el-form-item label="购买数量">
            <span>{{ selectedQuantity }}</span>
        </el-form-item>
        <el-form-item label="订单金额">
            <span style="color:#f56c6c;font-weight:bold;">¥ {{ previewTotal }}</span>
        </el-form-item>
        <el-form-item label="收货人" prop="receiverName">
            <el-input v-model="orderForm.receiverName"></el-input>
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
            <el-input v-model="orderForm.phone"></el-input>
        </el-form-item>
        <el-form-item label="收货地址" prop="address">
            <el-input v-model="orderForm.address"></el-input>
        </el-form-item>
        <el-form-item label="支付方式" prop="paymentMethod">
            <el-select v-model="orderForm.paymentMethod" placeholder="请选择支付方式">
                <el-option label="微信支付" value="微信支付"></el-option>
                <el-option label="支付宝" value="支付宝"></el-option>
                <el-option label="银行卡" value="银行卡"></el-option>
            </el-select>
        </el-form-item>
    </el-form>
    <div slot="footer">
        <el-button @click="dialogVisible = false" :disabled="submitting">取消</el-button>
        <el-button type="primary" @click="submitOrder" 
                   :loading="submitting" :disabled="submitting">
            {{ submitting ? '提交中...' : '提交订单' }}
        </el-button>
    </div>
</el-dialog>
```

### 2.3 后端订单创建逻辑

**文件位置**: `OrderService.java:39-114` (核心创建订单方法)

```java
@Transactional
public Order createOrder(CreateOrderRequest request, Long userId) {
    // 1. 参数校验
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
    
    // 2. 手机号格式验证
    String phone = request.getPhone().trim();
    if (!phone.matches("^1[3-9]\\d{9}$")) {
        throw new RuntimeException("手机号码格式不正确，必须为11位数字");
    }
    
    // 3. 获取用户信息
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("用户不存在"));
    
    // 4. 创建订单对象
    Order order = new Order();
    order.setUser(user);
    order.setOrderNo(generateOrderNo());  // 生成唯一订单号
    order.setStatus("PENDING_PAYMENT");   // 初始状态:待支付
    order.setAddress(request.getAddress().trim());
    order.setPhone(phone);
    order.setReceiverName(request.getReceiverName().trim());
    order.setPaymentMethod(request.getPaymentMethod());
    order.setCreateTime(new Date());
    order.setUpdateTime(new Date());
    
    // 5. 计算订单总金额并验证商品
    BigDecimal totalAmount = BigDecimal.ZERO;
    for (CreateOrderItemRequest itemRequest : request.getItems()) {
        // 验证商品ID和数量
        if (itemRequest.getProductId() == null) {
            throw new RuntimeException("商品不能为空");
        }
        if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
            throw new RuntimeException("商品数量必须大于0");
        }
        
        // 查询商品信息
        Product product = productRepository.findById(itemRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 验证商品状态
        if (!Boolean.TRUE.equals(product.getIsOnSale())) {
            throw new RuntimeException("商品未上架，无法下单");
        }
        
        // 验证库存
        if (product.getStock() == null || product.getStock() < itemRequest.getQuantity()) {
            throw new RuntimeException("商品库存不足: " + product.getName());
        }
        
        // 计算小计
        BigDecimal subtotal = product.getPrice()
            .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));
        totalAmount = totalAmount.add(subtotal);
    }
    
    // 6. 保存订单
    order.setTotalAmount(totalAmount);
    Order savedOrder = orderRepository.save(order);
    
    // 7. 创建订单项并扣减库存
    for (CreateOrderItemRequest itemRequest : request.getItems()) {
        Product product = productRepository.findById(itemRequest.getProductId())
            .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        // 创建订单项
        OrderItem orderItem = new OrderItem();
        orderItem.setOrder(savedOrder);
        orderItem.setProduct(product);
        orderItem.setProductName(product.getName());
        orderItem.setQuantity(itemRequest.getQuantity());
        orderItem.setPrice(product.getPrice());
        orderItem.setSubtotal(product.getPrice()
            .multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        orderItemRepository.save(orderItem);
        
        // 扣减库存
        product.setStock(product.getStock() - itemRequest.getQuantity());
        productRepository.save(product);
    }
    
    return savedOrder;
}
```

### 2.4 订单创建时序图说明

```
用户 -> 前端: 点击"立即下单"
前端 -> 前端: 打开订单确认弹窗
用户 -> 前端: 填写收货信息
前端 -> 前端: 表单验证(收货人、电话、地址)
用户 -> 前端: 点击"提交订单"
前端 -> 前端: 禁用按钮,显示loading
前端 -> 后端: POST /api/order (订单数据)
后端 -> 后端: 参数校验
后端 -> 数据库: 查询用户信息
数据库 -> 后端: 返回用户数据
后端 -> 数据库: 查询商品信息
数据库 -> 后端: 返回商品数据
后端 -> 后端: 验证商品状态和库存
后端 -> 后端: 计算订单总金额
后端 -> 数据库: 保存订单
数据库 -> 后端: 返回订单ID
后端 -> 数据库: 保存订单项
后端 -> 数据库: 扣减商品库存
后端 -> 前端: 返回订单创建成功
前端 -> 前端: 显示成功提示
前端 -> 前端: 跳转到订单列表页面
```

---

## 三、农户商品管理功能

### 3.1 功能描述
农户进入商品管理界面后可对自己上传的商品进行添加、编辑、删除操作。发布和编辑商品功能要求农户添加商品名称、分类、价格、库存、库存单位、商品描述及商品图片信息。其中编辑商品功能支持从本地读取图片文件并上传到服务器。

### 3.2 后端商品管理接口

**文件位置**: `ProductController.java:60-73` (创建商品)

```java
@PostMapping
public Product createProduct(@RequestBody Product product, HttpSession session) {
    // 1. 获取当前登录用户
    User currentUser = getCurrentUser(session);
    String role = getCurrentRole(session);
    
    // 2. 权限校验:仅管理员和农户可创建商品
    if (!isAdmin(role) && !isFarmer(role)) {
        throw new RuntimeException("无权限创建商品");
    }
    
    // 3. 设置商品所属农户
    if (isFarmer(role)) {
        product.setFarmer(currentUser);  // 农户创建的商品归属自己
    } else if (product.getFarmer() != null && product.getFarmer().getId() != null) {
        // 管理员可以指定农户
        product.setFarmer(userService.getUserById(product.getFarmer().getId()));
    }
    
    return productService.createProduct(product);
}
```

**文件位置**: `ProductController.java:75-95` (编辑商品)

```java
@PutMapping("/{id}")
public Product updateProduct(@PathVariable Long id, 
                            @RequestBody Product product, 
                            HttpSession session) {
    // 1. 获取当前用户和角色
    User currentUser = getCurrentUser(session);
    String role = getCurrentRole(session);
    
    // 2. 查询现有商品
    Product existing = productService.getProductById(id);
    
    // 3. 权限校验:农户只能修改自己的商品
    if (isFarmer(role) && 
        (existing.getFarmer() == null || 
         !currentUser.getId().equals(existing.getFarmer().getId()))) {
        throw new RuntimeException("无权限修改该商品");
    }
    
    // 4. 权限校验:普通用户不能修改商品
    if (!isAdmin(role) && !isFarmer(role)) {
        throw new RuntimeException("无权限修改商品");
    }
    
    // 5. 更新商品信息
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
```

**文件位置**: `ProductController.java:97-109` (删除商品)

```java
@DeleteMapping("/{id}")
public void deleteProduct(@PathVariable Long id, HttpSession session) {
    // 1. 获取当前用户和角色
    User currentUser = getCurrentUser(session);
    String role = getCurrentRole(session);
    
    // 2. 查询现有商品
    Product existing = productService.getProductById(id);
    
    // 3. 权限校验:农户只能删除自己的商品
    if (isFarmer(role) && 
        (existing.getFarmer() == null || 
         !currentUser.getId().equals(existing.getFarmer().getId()))) {
        throw new RuntimeException("无权限删除该商品");
    }
    
    // 4. 权限校验:普通用户不能删除商品
    if (!isAdmin(role) && !isFarmer(role)) {
        throw new RuntimeException("无权限删除商品");
    }
    
    // 5. 执行删除
    productService.deleteProduct(id);
}
```

### 3.3 Service层实现

**文件位置**: `ProductService.java:17-22` (创建商品)

```java
public Product createProduct(Product product) {
    // 应用默认值(如viewCount=0, salesCount=0, isOnSale=true)
    applyDefaultProductValues(product);
    
    // 设置创建和更新时间
    product.setCreateTime(new Date());
    product.setUpdateTime(new Date());
    
    // 保存到数据库
    return productRepository.save(product);
}
```

**文件位置**: `ProductService.java:49-60` (更新商品)

```java
public Product updateProduct(Product product) {
    // 1. 获取现有商品
    Product existingProduct = getProductById(product.getId());
    
    // 2. 保留创建时间,更新修改时间
    product.setCreateTime(existingProduct.getCreateTime());
    product.setUpdateTime(new Date());
    
    // 3. 保留未修改的字段
    if (product.getDescription() == null) {
        product.setDescription(existingProduct.getDescription());
    }
    if (product.getImages() == null) {
        product.setImages(existingProduct.getImages());
    }
    
    // 4. 保存更新
    return productRepository.save(product);
}
```

### 3.4 权限隔离机制说明

农户商品管理功能实现了严格的数据权限隔离:

1. **创建权限**: 仅农户和管理员可创建商品,普通用户无权限
2. **所有权绑定**: 农户创建的商品自动绑定到该农户账号
3. **修改权限**: 农户只能修改自己发布的商品,通过`product.getFarmer().getId()`校验
4. **删除权限**: 农户只能删除自己发布的商品,防止误删他人商品
5. **查询隔离**: 农户查询商品列表时只返回自己的商品(`/api/product/my`)

---

## 四、农户订单处理功能

### 4.1 功能描述
农户可以在订单管理侧边栏跳转订单管理界面,查看用户在商城下单并发送的订单。农户点击发货并在弹窗确认发货后,订单状态将更改为已发货;点击完成并在弹窗确认后,订单状态更改为已完成。

### 4.2 后端订单状态更新

**文件位置**: `OrderService.java:154-174` (农户更新订单状态)

```java
@Transactional
public Order farmerUpdateOrderStatus(Long id, Long farmerId, String status) {
    // 1. 权限校验:验证农户是否有权处理该订单
    if (!farmerCanAccessOrder(id, farmerId)) {
        throw new RuntimeException("无权限处理该订单");
    }
    
    // 2. 获取订单信息
    Order order = getOrderById(id);
    
    // 3. 状态流转校验
    if ("SHIPPED".equals(status)) {
        // 发货操作:只有已支付订单才能发货
        if (!"PAID".equals(order.getStatus())) {
            throw new RuntimeException("只有已支付订单才能发货");
        }
    } else if ("COMPLETED".equals(status)) {
        // 完成操作:只有已发货订单才能完成
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new RuntimeException("只有已发货订单才能完成");
        }
    } else {
        // 农户只能执行发货或完成操作
        throw new RuntimeException("农户只能执行发货或完成操作");
    }
    
    // 4. 更新订单状态
    order.setStatus(status);
    order.setUpdateTime(new Date());
    
    return orderRepository.save(order);
}
```

### 4.3 订单状态流转图

```
待支付(PENDING_PAYMENT)
    ↓ (用户支付)
已支付(PAID)
    ↓ (农户发货)
已发货(SHIPPED)
    ↓ (农户确认完成)
已完成(COMPLETED)
```

**状态流转规则**:
- 农户只能对"已支付"订单执行"发货"操作
- 农户只能对"已发货"订单执行"完成"操作
- 每次状态变更都会更新`updateTime`字段
- 使用`@Transactional`确保状态更新的原子性

### 4.4 权限校验机制

**文件位置**: `OrderService.java` (农户订单权限校验)

```java
public boolean farmerCanAccessOrder(Long orderId, Long farmerId) {
    // 查询订单
    Order order = getOrderById(orderId);
    
    // 遍历订单项,检查是否包含该农户的商品
    for (OrderItem item : order.getItems()) {
        if (item.getProduct() != null && 
            item.getProduct().getFarmer() != null &&
            farmerId.equals(item.getProduct().getFarmer().getId())) {
            return true;  // 订单中包含该农户的商品,有权访问
        }
    }
    
    return false;  // 订单中不包含该农户的商品,无权访问
}
```

**权限隔离说明**:
- 农户只能查看和处理包含自己商品的订单
- 通过订单项(OrderItem)关联商品(Product)进行权限校验
- 确保农户A无法查看或处理只包含农户B商品的订单

---

## 五、技术特点总结

### 5.1 安全性设计
1. **权限细粒度控制**: 基于角色的三级权限体系(管理员/农户/普通用户)
2. **数据权限隔离**: 农户只能管理自己的商品和相关订单
3. **参数校验**: 所有输入参数进行严格验证(非空、格式、范围)
4. **事务管理**: 使用`@Transactional`确保数据一致性

### 5.2 性能优化
1. **分页查询**: 使用Spring Data JPA的`Pageable`实现分页
2. **搜索防抖**: 前端输入框使用500ms防抖,减少API请求
3. **图片懒加载**: 使用IntersectionObserver API延迟加载图片

### 5.3 用户体验
1. **防重复提交**: 按钮禁用+loading状态,防止重复点击
2. **实时反馈**: 操作成功/失败立即显示提示信息
3. **表单验证**: 前端实时验证,后端二次校验

---

**文档版本**: v1.0  
**创建日期**: 2026-05-03  
**用途**: 论文关键代码展示
