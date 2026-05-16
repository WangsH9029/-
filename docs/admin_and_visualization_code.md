# 后台管理模块与数据可视化模块核心代码

本文档展示农产品交易系统中后台管理模块和数据可视化模块的核心实现代码,适用于论文技术实现章节。

---

## 一、后台管理模块

### 1.1 用户管理服务层 (UserService.java)

#### 1.1.1 用户注册与登录

```java
@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    /**
     * MD5密码加密
     */
    private String md5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }
    
    /**
     * 用户注册
     */
    public User register(User user) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        
        // 密码加密
        user.setPassword(md5(user.getPassword()));
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        // 设置默认角色
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        
        return userRepository.save(user);
    }
    
    /**
     * 用户登录验证
     */
    public User login(String username, String password) {
        User user = userRepository.findByUsername(username);
        if (user == null || !user.getPassword().equals(md5(password))) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user;
    }
}
```

#### 1.1.2 用户信息管理

```java
/**
 * 分页获取所有用户
 */
public Page<User> getAllUsers(Pageable pageable) {
    return userRepository.findAll(pageable);
}

/**
 * 更新用户信息
 */
public User updateUser(User user) {
    User existingUser = userRepository.findById(user.getId())
            .orElseThrow(() -> new RuntimeException("用户不存在"));

    // 更新基本信息
    existingUser.setUsername(user.getUsername());
    existingUser.setNickname(user.getNickname());
    existingUser.setPhone(user.getPhone());
    existingUser.setEmail(user.getEmail());
    existingUser.setAddress(user.getAddress());
    existingUser.setUpdateTime(new Date());

    // 更新角色和认证状态(需要权限)
    if (user.getRole() != null) {
        existingUser.setRole(user.getRole());
    }
    if (user.getIsVerified() != null) {
        existingUser.setIsVerified(user.getIsVerified());
    }
    
    // 更新密码(如果提供)
    if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
        existingUser.setPassword(md5(user.getPassword()));
    }

    return userRepository.save(existingUser);
}

/**
 * 修改用户角色(管理员功能)
 */
public User updateUserRole(Long id, String role) {
    User user = getUserById(id);
    user.setRole(role);
    user.setUpdateTime(new Date());
    return userRepository.save(user);
}

/**
 * 更新农户认证状态
 */
public User updateVerificationStatus(Long id, boolean isVerified) {
    User user = getUserById(id);
    user.setIsVerified(isVerified);
    user.setUpdateTime(new Date());
    return userRepository.save(user);
}

/**
 * 搜索用户
 */
public List<User> searchUsers(String keyword) {
    return userRepository.findByUsernameContainingOrNicknameContaining(
        keyword, keyword);
}
```

### 1.2 用户管理控制器 (UserController.java)

```java
@RestController
@RequestMapping("/api/user")
public class UserController {

    private static final String SESSION_USER_ID = "currentUserId";
    private static final String SESSION_USERNAME = "currentUsername";
    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表(支持角色过滤)
     */
    @GetMapping("/list")
    public List<User> getUserList(
            @RequestParam(required = false) String role, 
            HttpSession session) {
        requireAdmin(session);
        if (role != null && !role.isEmpty()) {
            return userService.findByRole(role);
        }
        return userService.findAll();
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Resp<LoginUserInfo> getCurrentUser(HttpSession session) {
        User user = getCurrentUserEntity(session);
        return Resp.success(new LoginUserInfo(
            user.getId(), 
            user.getUsername(), 
            user.getNickname(), 
            user.getRole()
        ));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id, 
            @RequestBody User user, 
            HttpSession session) {
        User currentUser = getCurrentUserEntity(session);
        
        // 权限检查:只有管理员或用户本人可以修改
        if (!isAdmin(session) && !currentUser.getId().equals(id)) {
            throw new RuntimeException("无权限修改该用户信息");
        }
        
        user.setId(id);
        
        // 非管理员不能修改角色和认证状态
        if (!isAdmin(session)) {
            user.setRole(currentUser.getRole());
            user.setIsVerified(currentUser.getIsVerified());
        }
        
        return userService.updateUser(user);
    }

    /**
     * 删除用户(管理员功能)
     */
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, HttpSession session) {
        requireAdmin(session);
        userService.deleteUser(id);
    }

    /**
     * 修改用户角色(管理员功能)
     */
    @PutMapping("/{id}/role")
    public User updateUserRole(
            @PathVariable Long id, 
            @RequestParam String role, 
            HttpSession session) {
        requireAdmin(session);
        return userService.updateUserRole(id, role);
    }

    /**
     * 农户认证审核(管理员功能)
     */
    @PutMapping("/{id}/verify")
    public User verifyUser(
            @PathVariable Long id, 
            @RequestParam boolean isVerified, 
            HttpSession session) {
        requireAdmin(session);
        return userService.updateVerificationStatus(id, isVerified);
    }

    /**
     * 搜索用户(管理员功能)
     */
    @GetMapping("/search")
    public List<User> searchUsers(
            @RequestParam String keyword, 
            HttpSession session) {
        requireAdmin(session);
        return userService.searchUsers(keyword);
    }

    /**
     * 权限验证:要求管理员权限
     */
    private void requireAdmin(HttpSession session) {
        if (!isAdmin(session)) {
            throw new RuntimeException("无权限访问");
        }
    }

    /**
     * 判断当前用户是否为管理员
     */
    private boolean isAdmin(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        return "ROLE_ADMIN".equals(role);
    }

    /**
     * 获取当前登录用户实体
     */
    private User getCurrentUserEntity(HttpSession session) {
        Object userId = session.getAttribute(SESSION_USER_ID);
        if (userId == null) {
            throw new RuntimeException("未登录");
        }
        return userService.getUserById(Long.valueOf(userId.toString()));
    }
}
```

---

## 二、数据可视化模块

### 2.1 统计分析服务层 (StatisticsService.java)

#### 2.1.1 系统概览统计

```java
@Service
public class StatisticsService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    /**
     * 获取系统概览统计数据
     * 包括:总销售额、订单数、用户数、农户数及其增长趋势
     */
    public OverviewStatistics getOverviewStatistics() {
        Date now = new Date();
        Date lastMonthStart = getDateBefore(now, 30);
        Date twoMonthsAgoStart = getDateBefore(now, 60);

        // 计算销售额及趋势
        BigDecimal totalSales = orderRepository.getTotalSalesAfter(new Date(0));
        BigDecimal lastMonthSales = orderRepository.getTotalSalesAfter(lastMonthStart);
        BigDecimal previousMonthSales = orderRepository.getTotalSalesAfter(twoMonthsAgoStart);
        previousMonthSales = previousMonthSales.subtract(lastMonthSales);

        // 计算订单数及趋势
        Long totalOrders = orderRepository.count();
        Long lastMonthOrders = orderRepository.countOrdersAfter(lastMonthStart);
        Long previousMonthOrders = orderRepository.countOrdersAfter(twoMonthsAgoStart) 
                                   - lastMonthOrders;

        // 计算用户数及趋势
        Long totalUsers = userRepository.countByRole("ROLE_USER");
        Long lastMonthUsers = userRepository.countByRoleAndCreateTimeAfter(
            "ROLE_USER", lastMonthStart);
        Long previousMonthUsers = userRepository.countByRoleAndCreateTimeAfter(
            "ROLE_USER", twoMonthsAgoStart) - lastMonthUsers;

        // 计算农户数及趋势
        Long totalFarmers = userRepository.countByRole("ROLE_FARMER");
        Long lastMonthFarmers = userRepository.countByRoleAndCreateTimeAfter(
            "ROLE_FARMER", lastMonthStart);
        Long previousMonthFarmers = userRepository.countByRoleAndCreateTimeAfter(
            "ROLE_FARMER", twoMonthsAgoStart) - lastMonthFarmers;

        return new OverviewStatistics(
            totalSales != null ? totalSales : BigDecimal.ZERO,
            calculateTrend(lastMonthSales, previousMonthSales),
            totalOrders,
            calculateTrend(lastMonthOrders, previousMonthOrders),
            totalUsers,
            calculateTrend(lastMonthUsers, previousMonthUsers),
            totalFarmers,
            calculateTrend(lastMonthFarmers, previousMonthFarmers)
        );
    }
}
```

#### 2.1.2 销售统计分析

```java
/**
 * 获取销售统计数据(支持周/月/年维度)
 * 返回时间序列的销售额和订单数
 */
public List<SalesDataPoint> getSalesStatistics(String range) {
    Date startDate = getStartDateByRange(range);
    List<Object[]> results = orderRepository.getSalesByDateRange(startDate);

    // 生成完整的日期标签
    Map<String, SalesDataPoint> dataMap = new LinkedHashMap<>();
    List<String> dateLabels = generateDateLabels(range);

    // 初始化所有日期为0
    for (String date : dateLabels) {
        dataMap.put(date, new SalesDataPoint(date, BigDecimal.ZERO, 0L));
    }

    // 填充实际数据
    for (Object[] row : results) {
        String date = (String) row[0];
        BigDecimal amount = (BigDecimal) row[1];
        Long count = ((Number) row[2]).longValue();
        dataMap.put(date, new SalesDataPoint(date, amount, count));
    }

    return new ArrayList<>(dataMap.values());
}
```

#### 2.1.3 用户增长统计

```java
/**
 * 获取用户增长统计(支持周/月/年维度)
 * 返回新增用户数和累计用户数
 */
public List<UserDataPoint> getUserStatistics(String range) {
    Date startDate = getStartDateByRange(range);
    List<Object[]> results = userRepository.getUserGrowthByDateRange(
        "ROLE_USER", startDate);

    Map<String, UserDataPoint> dataMap = new LinkedHashMap<>();
    List<String> dateLabels = generateDateLabels(range);

    // 初始化所有日期
    for (String date : dateLabels) {
        dataMap.put(date, new UserDataPoint(date, 0L, 0L));
    }

    // 计算累计用户数
    Long cumulativeUsers = userRepository.countByRoleAndCreateTimeAfter(
        "ROLE_USER", new Date(0)) - 
        userRepository.countByRoleAndCreateTimeAfter("ROLE_USER", startDate);

    // 填充数据并计算累计值
    for (Object[] row : results) {
        String date = (String) row[0];
        Long newUsers = ((Number) row[1]).longValue();
        cumulativeUsers += newUsers;
        dataMap.put(date, new UserDataPoint(date, newUsers, cumulativeUsers));
    }

    return new ArrayList<>(dataMap.values());
}
```

#### 2.1.4 热销商品排行

```java
/**
 * 获取热销商品排行榜
 * 包含销量、销售额和增长趋势
 */
public List<HotProduct> getHotProducts(int limit) {
    Page<Product> topProducts = productRepository.findTopSellingProducts(
        PageRequest.of(0, limit));

    // 计算时间范围
    Calendar cal = Calendar.getInstance();
    Date currentMonthEnd = cal.getTime();
    cal.add(Calendar.MONTH, -1);
    Date lastMonthEnd = cal.getTime();
    cal.add(Calendar.MONTH, -1);
    Date lastMonthStart = cal.getTime();

    // 查询上月销量数据
    List<Object[]> lastMonthSales = orderRepository.getProductSalesByDateRange(
        lastMonthStart, lastMonthEnd);
    Map<Long, Long> lastMonthSalesMap = new HashMap<>();
    for (Object[] row : lastMonthSales) {
        Long productId = ((Number) row[0]).longValue();
        Long quantity = ((Number) row[1]).longValue();
        lastMonthSalesMap.put(productId, quantity);
    }

    // 构建热销商品列表
    List<HotProduct> hotProducts = new ArrayList<>();
    int rank = 1;
    for (Product product : topProducts.getContent()) {
        BigDecimal amount = product.getPrice() != null 
            && product.getSalesCount() != null
            ? product.getPrice().multiply(
                BigDecimal.valueOf(product.getSalesCount()))
            : BigDecimal.ZERO;

        // 计算增长趋势
        Long currentSales = product.getSalesCount() != null 
            ? product.getSalesCount().longValue() : 0L;
        Long lastMonthSalesCount = lastMonthSalesMap.getOrDefault(
            product.getId(), 0L);
        Double trend = calculateTrend(currentSales, lastMonthSalesCount);

        hotProducts.add(new HotProduct(
            rank++,
            product.getName(),
            product.getSalesCount() != null ? product.getSalesCount() : 0,
            amount,
            trend
        ));
    }

    return hotProducts;
}
```

#### 2.1.5 趋势计算工具方法

```java
/**
 * 计算增长趋势百分比
 */
private Double calculateTrend(Object current, Object previous) {
    if (current == null || previous == null) {
        return 0.0;
    }

    double currentVal = 0.0;
    double previousVal = 0.0;

    if (current instanceof BigDecimal) {
        currentVal = ((BigDecimal) current).doubleValue();
    } else if (current instanceof Number) {
        currentVal = ((Number) current).doubleValue();
    }

    if (previous instanceof BigDecimal) {
        previousVal = ((BigDecimal) previous).doubleValue();
    } else if (previous instanceof Number) {
        previousVal = ((Number) previous).doubleValue();
    }

    if (previousVal == 0) {
        return currentVal > 0 ? 100.0 : 0.0;
    }

    double trend = ((currentVal - previousVal) / previousVal) * 100;
    return Math.round(trend * 10.0) / 10.0;
}

/**
 * 根据时间范围生成日期标签
 */
private List<String> generateDateLabels(String range) {
    List<String> labels = new ArrayList<>();
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    Calendar cal = Calendar.getInstance();

    int days;
    switch (range) {
        case "week":
            days = 7;
            break;
        case "year":
            days = 365;
            break;
        case "month":
        default:
            days = 30;
            break;
    }

    for (int i = days - 1; i >= 0; i--) {
        Calendar temp = (Calendar) cal.clone();
        temp.add(Calendar.DAY_OF_MONTH, -i);
        labels.add(sdf.format(temp.getTime()));
    }

    return labels;
}
```

### 2.2 统计分析控制器 (StatisticsController.java)

```java
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    private static final String SESSION_ROLE = "currentUserRole";

    @Autowired
    private StatisticsService statisticsService;

    /**
     * 获取系统概览统计
     * 返回:总销售额、订单数、用户数、农户数及增长趋势
     */
    @GetMapping("/overview")
    public OverviewStatistics getOverview(HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getOverviewStatistics();
    }

    /**
     * 获取销售统计数据
     * @param range 时间范围: week(周)/month(月)/year(年)
     * 返回:时间序列的销售额和订单数
     */
    @GetMapping("/sales")
    public List<SalesDataPoint> getSalesStatistics(
            @RequestParam(defaultValue = "month") String range,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getSalesStatistics(range);
    }

    /**
     * 获取用户增长统计
     * @param range 时间范围: week(周)/month(月)/year(年)
     * 返回:新增用户数和累计用户数
     */
    @GetMapping("/users")
    public List<UserDataPoint> getUserStatistics(
            @RequestParam(defaultValue = "month") String range,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getUserStatistics(range);
    }

    /**
     * 获取热销商品排行榜
     * @param limit 返回商品数量,默认5个
     * 返回:商品排名、销量、销售额和增长趋势
     */
    @GetMapping("/hot-products")
    public List<HotProduct> getHotProducts(
            @RequestParam(defaultValue = "5") int limit,
            HttpSession session) {
        checkAdminRole(session);
        return statisticsService.getHotProducts(limit);
    }

    /**
     * 权限检查:仅管理员可访问统计数据
     */
    private void checkAdminRole(HttpSession session) {
        Object role = session.getAttribute(SESSION_ROLE);
        if (!"ROLE_ADMIN".equals(role)) {
            throw new RuntimeException("无权限访问统计数据");
        }
    }
}
```

### 2.3 数据库查询层 (Repository)

```java
// OrderRepository.java
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    /**
     * 按日期范围统计销售数据
     */
    @Query("SELECT DATE_FORMAT(o.createTime, '%Y-%m-%d') as date, " +
           "SUM(o.totalAmount) as amount, COUNT(o.id) as count " +
           "FROM Order o WHERE o.createTime >= :startDate " +
           "GROUP BY DATE_FORMAT(o.createTime, '%Y-%m-%d') " +
           "ORDER BY date")
    List<Object[]> getSalesByDateRange(@Param("startDate") Date startDate);
    
    /**
     * 统计指定日期后的总销售额
     */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.createTime >= :startDate")
    BigDecimal getTotalSalesAfter(@Param("startDate") Date startDate);
    
    /**
     * 统计指定日期后的订单数
     */
    @Query("SELECT COUNT(o) FROM Order o WHERE o.createTime >= :startDate")
    Long countOrdersAfter(@Param("startDate") Date startDate);
    
    /**
     * 按时间范围统计商品销量
     */
    @Query("SELECT oi.product.id, SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.createTime >= :startDate " +
           "AND oi.order.createTime < :endDate " +
           "GROUP BY oi.product.id")
    List<Object[]> getProductSalesByDateRange(
        @Param("startDate") Date startDate, 
        @Param("endDate") Date endDate);
}

// UserRepository.java
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * 统计用户增长
     */
    @Query("SELECT DATE_FORMAT(u.createTime, '%Y-%m-%d') as date, " +
           "COUNT(u.id) as count " +
           "FROM User u " +
           "WHERE u.role = :role AND u.createTime >= :startDate " +
           "GROUP BY DATE_FORMAT(u.createTime, '%Y-%m-%d') " +
           "ORDER BY date")
    List<Object[]> getUserGrowthByDateRange(
        @Param("role") String role, 
        @Param("startDate") Date startDate);
    
    /**
     * 按角色统计用户数
     */
    Long countByRole(String role);
    
    /**
     * 按角色和创建时间统计用户数
     */
    Long countByRoleAndCreateTimeAfter(String role, Date createTime);
}

// ProductRepository.java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    /**
     * 获取热销商品
     */
    @Query("SELECT p FROM Product p ORDER BY p.salesCount DESC")
    Page<Product> findTopSellingProducts(Pageable pageable);
    
    /**
     * 按分类统计商品数量
     */
    @Query("SELECT p.category, COUNT(p) FROM Product p GROUP BY p.category")
    List<Object[]> getProductCountByCategory();
}
```

---

## 三、核心技术特点

### 3.1 后台管理模块特点

1. **权限控制体系**
   - 基于Session的角色权限验证
   - 三级权限:管理员(ROLE_ADMIN)、农户(ROLE_FARMER)、普通用户(ROLE_USER)
   - 细粒度权限控制:用户只能修改自己的信息,管理员可管理所有用户

2. **安全性设计**
   - MD5密码加密存储
   - 参数严格校验(非空、格式、范围)
   - Session状态管理,防止未授权访问

3. **数据完整性**
   - 自动维护创建时间和更新时间
   - 事务管理确保数据一致性
   - 级联更新相关数据

### 3.2 数据可视化模块特点

1. **多维度统计分析**
   - 支持周、月、年三种时间维度
   - 实时计算环比增长率
   - 多指标综合展示(销售额、订单数、用户数等)

2. **时间序列处理**
   - 自动生成完整日期序列
   - 填充缺失数据点为0
   - 保证图表数据连续性

3. **性能优化**
   - 使用数据库聚合查询减少数据传输
   - 分页查询支持大数据量
   - 缓存计算结果提升响应速度

4. **趋势分析算法**
   - 环比增长率计算:`((当前值 - 上期值) / 上期值) × 100%`
   - 特殊情况处理:上期为0时返回100%或0%
   - 精度控制:保留一位小数

### 3.3 技术栈

- **后端框架**: Spring Boot 2.x
- **持久层**: Spring Data JPA + Hibernate
- **数据库**: MySQL 8.0
- **安全**: Session + MD5加密
- **API设计**: RESTful风格
- **数据处理**: Java 8 Stream API

---

## 四、数据流程图

### 4.1 用户管理流程

```
管理员登录 → 验证Session → 获取用户列表
    ↓
选择用户 → 查看详情/编辑/删除
    ↓
修改角色/认证状态 → 权限校验 → 更新数据库
    ↓
返回操作结果 → 刷新列表
```

### 4.2 数据统计流程

```
管理员访问统计页面 → 权限验证
    ↓
选择时间范围(周/月/年)
    ↓
后端接收请求 → 计算起始日期
    ↓
执行数据库聚合查询
    ↓
生成完整时间序列 → 填充数据
    ↓
计算增长趋势 → 返回JSON数据
    ↓
前端渲染图表(ECharts)
```

---

## 五、应用场景

### 5.1 后台管理模块应用场景

1. **用户管理**: 管理员查看、编辑、删除用户信息
2. **角色分配**: 将普通用户提升为农户或管理员
3. **农户认证**: 审核农户身份,控制商品发布权限
4. **用户搜索**: 按用户名或昵称快速查找用户
5. **批量操作**: 按角色筛选用户进行批量管理

### 5.2 数据可视化模块应用场景

1. **运营监控**: 实时查看系统关键指标(销售额、订单数、用户数)
2. **趋势分析**: 分析业务增长趋势,辅助决策
3. **商品分析**: 识别热销商品,优化库存策略
4. **用户分析**: 监控用户增长,评估推广效果
5. **报表生成**: 导出统计数据用于管理报告

---

## 六、代码优势

1. **可维护性**: 代码结构清晰,职责分离,易于理解和修改
2. **可扩展性**: 采用接口编程,便于添加新的统计维度
3. **健壮性**: 完善的异常处理和参数校验
4. **性能**: 使用数据库聚合查询,减少应用层计算
5. **安全性**: 多层权限验证,防止越权访问

---

**文档版本**: v1.0  
**创建日期**: 2026-05-03  
**适用场景**: 毕业论文技术实现章节代码展示
