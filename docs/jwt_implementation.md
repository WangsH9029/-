# JWT Token验证机制实现说明

本文档详细说明了农产品交易系统中JWT Token验证机制的实现。

---

## 📋 功能概述

JWT (JSON Web Token) 是一种基于Token的身份认证机制,相比传统的Session机制具有以下优势:

- **无状态**: 服务器不需要存储Session,支持分布式部署
- **跨域友好**: Token可以在不同域名间传递
- **移动端友好**: 适合移动应用和单页应用
- **安全性高**: Token包含签名,防止篡改

---

## 🏗️ 系统架构

### 认证流程

```
1. 用户登录 → 2. 服务器验证 → 3. 生成JWT Token → 4. 返回Token给前端
                                                              ↓
5. 前端存储Token到localStorage ← ← ← ← ← ← ← ← ← ← ← ← ← ← ←
                ↓
6. 后续请求携带Token (Header: Authorization: Bearer {token})
                ↓
7. JWT拦截器验证Token → 8. 提取用户信息 → 9. 权限拦截器校验角色 → 10. 访问资源
```

---

## 📁 文件结构

```
src/main/java/com/ywtong/springboothtml/
├── util/
│   └── JwtUtil.java                    # JWT工具类(生成和解析Token)
├── interceptor/
│   ├── JwtInterceptor.java             # JWT拦截器(验证Token)
│   └── RoleInterceptor.java            # 权限拦截器(已更新支持JWT)
├── config/
│   └── WebMvcConfig.java               # 拦截器配置
├── controller/
│   └── IndexController.java            # 登录接口(已更新返回Token)
└── entity/
    └── LoginUserInfo.java              # 登录响应实体(已添加token字段)

src/main/resources/
├── static/js/
│   └── axios-config.js                 # Axios配置(自动携带Token)
└── templates/
    └── index.html                      # 登录页面(保存Token到localStorage)
```

---

## 🔧 核心实现

### 1. JWT工具类 (`JwtUtil.java`)

**位置**: `src/main/java/com/ywtong/springboothtml/util/JwtUtil.java`

**功能**:
- 生成JWT Token
- 解析JWT Token
- 验证Token有效性
- 提取用户信息(userId, username, role)

**关键方法**:

```java
// 生成Token (有效期2小时)
public String generateToken(Long userId, String username, String role)

// 解析Token
public Claims parseToken(String token)

// 验证Token是否有效
public boolean validateToken(String token)

// 从Token中获取用户信息
public Long getUserIdFromToken(String token)
public String getUsernameFromToken(String token)
public String getRoleFromToken(String token)
```

**Token结构**:
```json
{
  "userId": 1,
  "username": "admin",
  "role": "ROLE_ADMIN",
  "sub": "admin",
  "iat": 1714665600,
  "exp": 1714672800
}
```

---

### 2. JWT拦截器 (`JwtInterceptor.java`)

**位置**: `src/main/java/com/ywtong/springboothtml/interceptor/JwtInterceptor.java`

**功能**:
- 从请求头中提取Token (`Authorization: Bearer {token}`)
- 验证Token有效性
- 将用户信息存入request attribute供后续使用

**拦截逻辑**:

```java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    // 1. 从Header获取Token
    String token = request.getHeader("Authorization");
    
    // 2. 验证Token格式
    if (token == null || !token.startsWith("Bearer ")) {
        return false; // 401 未登录
    }
    
    // 3. 验证Token有效性
    if (!jwtUtil.validateToken(token)) {
        return false; // 401 Token过期
    }
    
    // 4. 提取用户信息并存入request
    request.setAttribute("currentUserId", userId);
    request.setAttribute("currentUserRole", role);
    
    return true;
}
```

---

### 3. 登录接口更新 (`IndexController.java`)

**位置**: `src/main/java/com/ywtong/springboothtml/controller/IndexController.java:32-51`

**修改内容**:

```java
@PostMapping("/login")
public Resp<LoginUserInfo> login(...) {
    User user = userService.login(account, password);
    
    // 生成JWT Token
    String token = jwtUtil.generateToken(
        user.getId(), 
        user.getUsername(), 
        user.getRole()
    );
    
    // 返回Token给前端
    LoginUserInfo loginUserInfo = new LoginUserInfo(...);
    loginUserInfo.setToken(token);
    
    return Resp.success(loginUserInfo);
}
```

**响应示例**:
```json
{
  "code": "200",
  "msg": "登录成功",
  "body": {
    "userId": 1,
    "username": "admin",
    "role": "ROLE_ADMIN",
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlIjoiUk9MRV9BRE1JTiJ9.xxx"
  }
}
```

---

### 4. 前端Token管理

#### 4.1 Axios请求拦截器 (`axios-config.js:12-20`)

**功能**: 自动在所有请求头中携带Token

```javascript
axiosInstance.interceptors.request.use(config => {
    // 从localStorage获取Token
    const token = localStorage.getItem('jwt_token');
    if (token) {
        // 添加到请求头
        config.headers['Authorization'] = 'Bearer ' + token;
    }
    return config;
});
```

#### 4.2 登录页面保存Token (`index.html:216-251`)

**功能**: 登录成功后保存Token到localStorage

```javascript
success: (response) => {
    if (response.body.token) {
        // 保存Token到localStorage
        localStorage.setItem('jwt_token', response.body.token);
    }
    // 跳转到对应页面
    window.location.href = 'toMain';
}
```

#### 4.3 Token过期处理 (`axios-config.js:36-45`)

**功能**: Token过期时清除并跳转登录页

```javascript
case 401:
    errorMessage = data?.message || '未授权，请重新登录';
    // 清除过期的Token
    localStorage.removeItem('jwt_token');
    setTimeout(() => {
        window.location.href = '/demo/';
    }, 1500);
    break;
```

---

### 5. 权限拦截器更新 (`RoleInterceptor.java`)

**位置**: `src/main/java/com/ywtong/springboothtml/interceptor/RoleInterceptor.java:20-36`

**修改内容**: 支持从JWT Token或Session中获取用户信息

```java
@Override
public boolean preHandle(HttpServletRequest request, ...) {
    // 优先从request attribute获取(JWT拦截器设置的)
    String role = (String) request.getAttribute("currentUserRole");
    Object userId = request.getAttribute("currentUserId");
    
    // 如果JWT中没有,则从Session获取(兼容旧方式)
    if (role == null || userId == null) {
        HttpSession session = request.getSession();
        role = (String) session.getAttribute("currentUserRole");
        userId = session.getAttribute("currentUserId");
    }
    
    // 后续权限校验逻辑...
}
```

---

### 6. 拦截器配置 (`WebMvcConfig.java`)

**位置**: `src/main/java/com/ywtong/springboothtml/config/WebMvcConfig.java:31-60`

**配置说明**:

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    // JWT拦截器 (优先级1,最先执行)
    registry.addInterceptor(jwtInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/api/user/register",
                "/api/user/send-code",
                "/api/user/reset-password",
                "/api/product/search",
                "/api/product/{id}"
            )
            .order(1);
    
    // 权限拦截器 (优先级2,在JWT之后执行)
    registry.addInterceptor(roleInterceptor)
            .addPathPatterns("/api/**")
            .excludePathPatterns(...)
            .order(2);
}
```

**执行顺序**:
1. JWT拦截器验证Token → 提取用户信息
2. 权限拦截器校验角色 → 判断是否有权限访问

---

## 🔐 安全特性

### 1. Token签名验证
- 使用HMAC-SHA256算法签名
- 密钥长度: 256位
- 防止Token被篡改

### 2. Token过期机制
- 有效期: 2小时
- 过期后自动失效,需要重新登录
- 前端自动清除过期Token

### 3. 请求头验证
- Token必须在`Authorization`头中
- 格式必须为`Bearer {token}`
- 格式错误返回401

### 4. 双层拦截保护
- JWT拦截器: 验证身份
- 权限拦截器: 验证权限
- 两层防护,安全性更高

---

## 🚀 启用JWT Token验证

**当前状态**: JWT功能已实现,但**默认未启用**,系统仍使用Session机制。

### 如何启用JWT

**步骤1**: 修改 `WebMvcConfig.java:31-60`

取消JWT拦截器的注释:

```java
// 取消下面的注释
registry.addInterceptor(jwtInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns(...)
        .order(1);
```

**步骤2**: 重启应用

```bash
mvn spring-boot:run
```

**步骤3**: 测试JWT功能

1. 清除浏览器localStorage中的旧Token
2. 重新登录,查看Network中的响应是否包含token字段
3. 查看后续请求的Header中是否包含`Authorization: Bearer {token}`

---

## 🔄 Session vs JWT 对比

| 特性 | Session机制 | JWT Token机制 |
|------|------------|---------------|
| **存储位置** | 服务器内存/Redis | 客户端localStorage |
| **状态** | 有状态(服务器存储) | 无状态(自包含) |
| **扩展性** | 单机或需要Session共享 | 天然支持分布式 |
| **性能** | 需要查询Session存储 | 无需查询,直接解析 |
| **安全性** | 依赖Cookie,易受CSRF攻击 | 存储在Header,防CSRF |
| **跨域** | 需要特殊配置 | 天然支持跨域 |
| **移动端** | 不友好 | 友好 |
| **过期控制** | 服务器控制 | Token自带过期时间 |

---

## 🧪 测试JWT功能

### 测试1: 登录获取Token

```bash
curl -X POST http://localhost:8080/demo/login \
  -d "account=admin&password=123456"
```

**预期响应**:
```json
{
  "code": "200",
  "body": {
    "token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

### 测试2: 使用Token访问接口

```bash
curl -X GET http://localhost:8080/demo/api/user/current \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

**预期响应**: 返回当前用户信息

### 测试3: Token过期

等待2小时后,使用过期Token访问接口:

**预期响应**:
```json
{
  "code": "401",
  "message": "Token已过期,请重新登录"
}
```

### 测试4: Token格式错误

```bash
curl -X GET http://localhost:8080/demo/api/user/current \
  -H "Authorization: InvalidToken"
```

**预期响应**:
```json
{
  "code": "401",
  "message": "未登录,请先登录"
}
```

---

## 📝 论文阐述建议

在论文中可以这样描述JWT功能:

> **Token登录验证机制**
> 
> 系统实现了基于JWT (JSON Web Token) 的身份认证机制,提供无状态的分布式认证方案:
> 
> **(1) Token生成与签名**: 用户登录成功后,服务器使用HMAC-SHA256算法生成包含用户ID、用户名、角色的JWT Token (有效期2小时),并返回给前端。Token包含数字签名,防止被篡改。
> 
> **(2) Token存储与传递**: 前端将Token存储在localStorage中,后续所有API请求通过Axios请求拦截器自动在Header中携带`Authorization: Bearer {token}`。
> 
> **(3) Token验证流程**: 后端通过JWT拦截器(JwtInterceptor)验证Token有效性,提取用户信息存入request attribute,供权限拦截器(RoleInterceptor)进行角色权限校验。
> 
> **(4) 双层拦截保护**: JWT拦截器负责身份验证,权限拦截器负责权限控制,两层防护确保接口安全。Token过期或格式错误时返回401状态码,前端自动清除Token并跳转登录页。
> 
> 相比传统Session机制,JWT方案具有无状态、支持分布式部署、跨域友好等优势,更适合现代Web应用和移动端应用。

---

## 🔧 配置说明

### Token有效期配置

修改 `JwtUtil.java:23`:

```java
// 默认2小时
private static final long EXPIRATION_TIME = 2 * 60 * 60 * 1000;

// 修改为24小时
private static final long EXPIRATION_TIME = 24 * 60 * 60 * 1000;
```

### 密钥配置

**生产环境建议**: 将密钥移到配置文件

1. 在 `application.yml` 中添加:
```yaml
jwt:
  secret: your-very-long-secret-key-here
  expiration: 7200000  # 2小时(毫秒)
```

2. 修改 `JwtUtil.java`:
```java
@Value("${jwt.secret}")
private String secretKey;

@Value("${jwt.expiration}")
private long expirationTime;
```

---

## ⚠️ 注意事项

### 1. 兼容性设计
- 系统同时支持JWT和Session两种机制
- 登录时同时设置Session和返回Token
- 权限拦截器优先使用JWT,降级到Session
- 可以平滑迁移,无需一次性切换

### 2. 安全建议
- **生产环境必须使用HTTPS**: 防止Token被窃取
- **定期更换密钥**: 建议每季度更换一次
- **Token刷新机制**: 可以实现refresh token延长有效期
- **黑名单机制**: 可以实现Token撤销功能

### 3. 性能优化
- Token验证是纯计算操作,无需查询数据库
- 相比Session查询,性能提升明显
- 适合高并发场景

---

## 🎯 后续优化方向

### 短期优化
1. **Token刷新机制**: 实现refresh token,避免频繁登录
2. **Token黑名单**: 支持主动撤销Token(退出登录)
3. **多设备管理**: 记录Token与设备的关联关系

### 长期优化
1. **Redis存储**: 将Token黑名单存储到Redis
2. **单点登录(SSO)**: 支持多个系统共享Token
3. **OAuth2集成**: 支持第三方登录(微信、支付宝)

---

**文档版本**: v1.0  
**创建日期**: 2026-05-02  
**作者**: 系统开发团队
