# 三项安全功能测试指南

## 测试环境准备

### 测试账号
需要准备三个不同角色的账号:
- **管理员账号**: admin / 密码
- **农户A账号**: farmer1 / 密码  
- **农户B账号**: farmer2 / 密码
- **普通用户账号**: user1 / 密码

---

## 测试1: 数据权限隔离

### 测试目标
验证农户只能管理自己的商品,不能修改其他农户的商品。

### 测试步骤

#### 步骤1: 农户A创建商品
1. 使用**农户A账号**登录
2. 访问 `/demo/toProductList` (商品管理页面)
3. 点击"添加商品",创建一个商品,记录商品ID (例如: id=10)
4. 确认商品列表中只显示自己创建的商品

#### 步骤2: 农户B尝试修改农户A的商品
1. 退出登录,使用**农户B账号**登录
2. 访问 `/demo/toProductList` (商品管理页面)
3. 确认看不到农户A的商品(只能看到自己的)
4. **关键测试**: 在浏览器地址栏手动输入:
   ```
   /demo/api/product/10
   ```
   (将10替换为农户A的商品ID)
5. 打开浏览器开发者工具(F12) → Network标签
6. 在控制台(Console)执行以下代码尝试修改:
   ```javascript
   fetch('/demo/api/product/10', {
       method: 'PUT',
       headers: {'Content-Type': 'application/json'},
       body: JSON.stringify({
           id: 10,
           name: '被篡改的商品名',
           price: 0.01
       })
   }).then(r => r.json()).then(console.log)
   ```
7. **预期结果**: 返回错误 "无权限修改该商品"

#### 步骤3: 验证订单数据隔离
1. 使用**普通用户账号**登录
2. 在商城购买农户A的商品,创建订单(记录订单ID,例如: id=5)
3. 退出登录,使用**农户B账号**登录
4. 在浏览器控制台执行:
   ```javascript
   fetch('/demo/api/order/5').then(r => r.json()).then(console.log)
   ```
5. **预期结果**: 返回错误 "无权限查看该订单"

### 测试结果判定
- ✅ **通过**: 农户B无法查看/修改农户A的商品和订单
- ❌ **失败**: 农户B能成功修改或查看其他农户的数据

---

## 测试2: 防重复提交

### 测试目标
验证快速点击提交按钮时,只会创建一个订单。

### 测试步骤

#### 方法1: 模拟网络延迟(推荐)
1. 使用**普通用户账号**登录
2. 访问 `/demo/toMall` (商城页面)
3. 打开浏览器开发者工具(F12) → Network标签
4. 点击Network标签右上角的"No throttling"下拉框
5. 选择"Slow 3G"或"Fast 3G"(模拟慢速网络)
6. 选择一个商品,点击"立即下单"
7. 填写订单信息
8. **快速连续点击"提交订单"按钮5-10次**
9. 观察按钮状态变化和Network请求数量

#### 方法2: 使用浏览器控制台
1. 在订单确认弹窗中,打开浏览器控制台(F12)
2. 在Console中执行以下代码:
   ```javascript
   // 模拟快速点击10次
   for(let i=0; i<10; i++) {
       document.querySelector('.el-dialog__footer .el-button--primary').click();
   }
   ```
3. 查看Network标签中的请求数量
4. 查看订单列表,确认只生成了一个订单

### 测试结果判定
- ✅ **通过**: 
  - 按钮点击后立即显示loading状态
  - 按钮变为禁用状态(灰色,无法点击)
  - Network中只有1个POST请求到 `/api/order`
  - 订单列表中只生成了1个订单
  - 3秒后按钮恢复可用状态

- ❌ **失败**: 
  - 生成了多个相同的订单
  - 按钮没有禁用,可以重复点击

---

## 测试3: 权限拦截器

### 测试目标
验证不同角色只能访问对应权限的接口。

### 测试步骤

#### 测试3.1: 未登录访问
1. 确保已退出登录(或使用无痕模式)
2. 在浏览器控制台执行:
   ```javascript
   fetch('/demo/api/product/my').then(r => r.json()).then(console.log)
   ```
3. **预期结果**: 返回 `{code: "401", message: "未登录,请先登录"}`

#### 测试3.2: 普通用户访问管理员接口
1. 使用**普通用户账号**登录
2. 在浏览器控制台执行:
   ```javascript
   // 尝试访问用户列表(仅管理员)
   fetch('/demo/api/user/list').then(r => r.json()).then(console.log)
   ```
3. **预期结果**: 返回 `{code: "403", message: "无权限访问,仅管理员可操作"}`

4. 尝试访问统计数据:
   ```javascript
   fetch('/demo/api/statistics/overview').then(r => r.json()).then(console.log)
   ```
5. **预期结果**: 返回 403错误

#### 测试3.3: 普通用户访问农户接口
1. 继续使用**普通用户账号**
2. 在浏览器控制台执行:
   ```javascript
   fetch('/demo/api/product/my').then(r => r.json()).then(console.log)
   ```
3. **预期结果**: 返回 `{code: "403", message: "无权限访问,仅农户可操作"}`

#### 测试3.4: 农户访问管理员接口
1. 退出登录,使用**农户账号**登录
2. 在浏览器控制台执行:
   ```javascript
   fetch('/demo/api/user/list').then(r => r.json()).then(console.log)
   ```
3. **预期结果**: 返回 403错误

4. 尝试删除用户:
   ```javascript
   fetch('/demo/api/user/1', {method: 'DELETE'}).then(r => r.json()).then(console.log)
   ```
5. **预期结果**: 返回 403错误

#### 测试3.5: 普通用户尝试创建商品
1. 使用**普通用户账号**登录
2. 在浏览器控制台执行:
   ```javascript
   fetch('/demo/api/product', {
       method: 'POST',
       headers: {'Content-Type': 'application/json'},
       body: JSON.stringify({
           name: '测试商品',
           price: 10,
           stock: 100
       })
   }).then(r => r.json()).then(console.log)
   ```
3. **预期结果**: 返回 `{code: "403", message: "无权限访问,普通用户不可操作"}`

#### 测试3.6: 手动输入URL跳转测试
1. 使用**普通用户账号**登录
2. 在浏览器地址栏手动输入:
   ```
   http://localhost:8080/demo/toProductList
   ```
3. 按回车访问
4. **预期结果**: 
   - **如果跳转到用户主页** → 这是IndexController的requireRole()方法的保护机制,也是正确的
   - **如果显示403错误页面** → 也是正确的
   - **如果能正常访问商品管理页面** → 这是错误的,说明页面级权限控制失效

### 测试结果判定
- ✅ **通过**: 所有越权访问都被拦截,返回401或403错误
- ❌ **失败**: 任何一个越权访问成功返回了数据

---

## 权限矩阵参考表

| 接口 | 管理员 | 农户 | 普通用户 |
|------|--------|------|----------|
| GET /api/user/list | ✅ | ❌ | ❌ |
| DELETE /api/user/{id} | ✅ | ❌ | ❌ |
| GET /api/statistics/* | ✅ | ❌ | ❌ |
| GET /api/product/my | ❌ | ✅ | ❌ |
| POST /api/product | ✅ | ✅ | ❌ |
| PUT /api/product/{id} | ✅ | ✅(仅自己的) | ❌ |
| DELETE /api/product/{id} | ✅ | ✅(仅自己的) | ❌ |
| GET /api/product/search | ✅ | ✅ | ✅ |
| POST /api/order | ✅ | ✅ | ✅ |
| GET /api/order/list | ✅(全部) | ✅(相关) | ✅(自己的) |

---

## 常见问题

### Q1: 为什么农户在商品管理页面看不到其他农户的商品?
**A**: 这是正确的设计。商品管理页面(`/toProductList`)是让农户管理**自己的商品**,不是浏览所有商品。如果要浏览所有商品,应该访问商城页面(`/toMall`)。

### Q2: 防重复提交测试时,点击太快看不到效果怎么办?
**A**: 使用Chrome开发者工具的Network throttling功能,选择"Slow 3G"模拟慢速网络,这样提交过程会变慢,更容易观察到按钮的loading状态。

### Q3: 手动输入URL被跳转是bug吗?
**A**: 不是bug。`IndexController.requireRole()`方法会检查用户角色,如果角色不匹配会自动跳转到对应的主页。这是页面级的权限保护,是正确的设计。

### Q4: 如何验证拦截器真的生效了?
**A**: 使用浏览器控制台直接调用API接口(绕过前端页面),如果返回401/403错误,说明拦截器生效了。前端页面的跳转是额外的保护层。

---

## 测试报告模板

```
测试日期: ____________________
测试人员: ____________________

【测试1: 数据权限隔离】
- 农户B修改农户A商品: □ 通过 □ 失败
- 农户B查看其他订单: □ 通过 □ 失败

【测试2: 防重复提交】
- 按钮loading状态: □ 通过 □ 失败
- 只生成一个订单: □ 通过 □ 失败

【测试3: 权限拦截器】
- 未登录访问: □ 通过 □ 失败
- 普通用户访问管理员接口: □ 通过 □ 失败
- 普通用户访问农户接口: □ 通过 □ 失败
- 农户访问管理员接口: □ 通过 □ 失败
- 普通用户创建商品: □ 通过 □ 失败

总体评价: ____________________
```
