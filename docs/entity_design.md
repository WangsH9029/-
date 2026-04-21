# 农产品电商系统实体属性设计

## 1. 用户实体（User）

用户实体是系统的核心实体之一，用于存储系统中所有用户的基本信息，包括普通用户、农户和管理员三种角色。

| 属性名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| id | Long | 用户唯一标识 | 主键，自增 |
| username | String | 登录账号 | 非空，唯一 |
| password | String | 登录密码（MD5加密） | 非空 |
| role | String | 用户角色 | 非空，取值：ROLE_USER/ROLE_FARMER/ROLE_ADMIN |
| nickname | String | 用户昵称 | - |
| phone | String | 联系电话 | - |
| email | String | 电子邮箱 | - |
| address | String | 联系地址 | - |
| isVerified | Boolean | 农户认证状态 | 仅农户角色使用 |
| createTime | Date | 创建时间 | - |
| updateTime | Date | 更新时间 | - |

**设计说明**：用户实体采用单表设计，通过 role 字段区分不同角色类型。isVerified 字段专门用于农户认证管理，当用户角色为 ROLE_FARMER 时，该字段标识农户是否通过平台认证。密码采用 MD5 加密存储，保障用户账户安全。

---

## 2. 商品实体（Product）

商品实体用于存储农产品的详细信息，包括商品基本属性、库存信息和销售统计数据。

| 属性名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| id | Long | 商品唯一标识 | 主键，自增 |
| name | String | 商品名称 | 非空 |
| description | String | 商品描述 | - |
| category | String | 商品分类 | 如：蔬菜、水果、粮油等 |
| price | BigDecimal | 商品单价 | 非空 |
| stock | Integer | 库存数量 | 非空 |
| unit | String | 计量单位 | 如：斤、个、箱等 |
| images | String | 商品图片URL | 多个图片用逗号分隔 |
| isOnSale | Boolean | 是否上架 | 默认true |
| viewCount | Integer | 浏览次数 | 默认0 |
| salesCount | Integer | 销售数量 | 默认0 |
| farmer | User | 发布农户 | 外键，关联User表 |
| createTime | Date | 创建时间 | - |
| updateTime | Date | 更新时间 | - |

**设计说明**：商品实体通过 @ManyToOne 注解与用户实体建立多对一关联关系，farmer 字段记录商品的发布农户。images 字段采用逗号分隔的字符串存储多张图片URL，简化数据库设计。viewCount 和 salesCount 字段用于统计商品热度和销量，支持热门商品推荐功能。

---

## 3. 订单实体（Order）

订单实体用于存储用户的购买订单信息，记录订单的完整生命周期状态。

| 属性名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| id | Long | 订单唯一标识 | 主键，自增 |
| orderNo | String | 订单编号 | 唯一，系统自动生成 |
| user | User | 下单用户 | 外键，关联User表 |
| totalAmount | BigDecimal | 订单总金额 | 非空 |
| status | String | 订单状态 | 取值：PENDING_PAYMENT/PAID/SHIPPED/COMPLETED/CANCELLED |
| address | String | 收货地址 | 非空 |
| phone | String | 联系电话 | 非空，长度11位 |
| receiverName | String | 收货人姓名 | - |
| paymentMethod | String | 支付方式 | 如：支付宝、微信支付等 |
| createTime | Date | 创建时间 | - |
| updateTime | Date | 更新时间 | - |

**设计说明**：订单实体通过 @ManyToOne 注解与用户实体建立关联，记录下单用户信息。status 字段实现订单状态机管理，订单状态流转为：待支付→已支付→已发货→已完成，或在待支付/已支付阶段可取消。orderNo 采用时间戳+随机数生成唯一订单编号。系统支持用户在未支付状态下修改收货地址和联系电话。

---

## 4. 订单明细实体（OrderItem）

订单明细实体用于存储订单中的商品详细信息，实现订单与商品的多对多关联。

| 属性名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| id | Long | 明细唯一标识 | 主键，自增 |
| order | Order | 所属订单 | 外键，关联Order表 |
| product | Product | 关联商品 | 外键，关联Product表 |
| productName | String | 商品名称快照 | 记录下单时商品名称 |
| quantity | Integer | 购买数量 | 非空 |
| price | BigDecimal | 商品单价快照 | 记录下单时单价 |
| subtotal | BigDecimal | 小计金额 | quantity × price |
| totalPrice | BigDecimal | 总价 | - |

**设计说明**：订单明细实体通过 @ManyToOne 注解分别与订单实体和商品实体建立关联。productName 和 price 字段作为商品信息快照，记录下单时的商品名称和价格，避免商品信息变更后影响历史订单数据。一个订单可包含多个订单明细，实现购物车批量下单功能。

---

## 5. 购物车实体（CartItem）

购物车实体用于存储用户临时选购的商品信息，支持用户在下单前管理购物清单。

| 属性名 | 数据类型 | 说明 | 约束 |
|--------|---------|------|------|
| id | Long | 购物车项唯一标识 | 主键，自增 |
| user | User | 所属用户 | 外键，关联User表 |
| product | Product | 关联商品 | 外键，关联Product表 |
| quantity | Integer | 购买数量 | 非空，默认1 |
| createTime | Date | 加入时间 | - |
| updateTime | Date | 更新时间 | - |

**设计说明**：购物车实体通过 @ManyToOne 注解分别与用户实体和商品实体建立关联。每个用户可拥有多个购物车项，每个购物车项对应一个商品及其购买数量。用户可在购物车中修改商品数量、删除商品或批量结算。购物车数据在用户下单后不会自动清空，需用户手动删除或系统定期清理。

---

## 实体关系说明

1. **User 与 Product**：一对多关系。一个农户（User）可以发布多个商品（Product），每个商品只能属于一个农户。

2. **User 与 Order**：一对多关系。一个用户（User）可以创建多个订单（Order），每个订单只能属于一个用户。

3. **Order 与 OrderItem**：一对多关系。一个订单（Order）可以包含多个订单明细（OrderItem），每个订单明细只能属于一个订单。

4. **Product 与 OrderItem**：一对多关系。一个商品（Product）可以出现在多个订单明细（OrderItem）中，每个订单明细只能关联一个商品。

5. **User 与 CartItem**：一对多关系。一个用户（User）可以拥有多个购物车项（CartItem），每个购物车项只能属于一个用户。

6. **Product 与 CartItem**：一对多关系。一个商品（Product）可以被多个用户加入购物车（CartItem），每个购物车项只能关联一个商品。

通过以上实体设计，系统实现了用户管理、商品管理、订单管理和购物车管理的完整业务流程，各实体间通过外键关联保证了数据的完整性和一致性。
