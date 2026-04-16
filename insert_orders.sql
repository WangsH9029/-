-- 插入订单数据用于数据统计展示
USE farm_market;

-- 插入订单（涵盖不同时间段、不同状态）
INSERT INTO `order` (user_id, order_no, total_amount, status, address, phone, receiver_name, payment_method, create_time, update_time) VALUES
-- 2026年1月的订单
(4, 'ORD202601150001', 119.70, 'COMPLETED', '北京市朝阳区建国路88号', '13800138001', '张三', '微信支付', '2026-01-15 10:30:00', '2026-01-16 14:20:00'),
(8, 'ORD202601180002', 75.00, 'COMPLETED', '上海市浦东新区世纪大道100号', '13900139002', '李四', '支付宝', '2026-01-18 14:20:00', '2026-01-19 09:15:00'),
(11, 'ORD202601220003', 199.50, 'COMPLETED', '广州市天河区天河路123号', '13700137003', '王五', '微信支付', '2026-01-22 16:45:00', '2026-01-23 11:30:00'),

-- 2026年2月的订单
(4, 'ORD202602050004', 89.80, 'COMPLETED', '北京市朝阳区建国路88号', '13800138001', '张三', '银行卡', '2026-02-05 09:15:00', '2026-02-06 10:20:00'),
(8, 'ORD202602100005', 156.00, 'COMPLETED', '上海市浦东新区世纪大道100号', '13900139002', '李四', '微信支付', '2026-02-10 11:30:00', '2026-02-11 15:40:00'),
(11, 'ORD202602140006', 218.70, 'SHIPPED', '广州市天河区天河路123号', '13700137003', '王五', '支付宝', '2026-02-14 13:20:00', '2026-02-15 08:30:00'),
(4, 'ORD202602200007', 63.00, 'COMPLETED', '北京市海淀区中关村大街1号', '13800138004', '赵六', '微信支付', '2026-02-20 15:50:00', '2026-02-21 10:15:00'),

-- 2026年3月的订单
(8, 'ORD202603020008', 125.00, 'COMPLETED', '深圳市南山区科技园路88号', '13900139005', '孙七', '支付宝', '2026-03-02 10:10:00', '2026-03-03 14:25:00'),
(11, 'ORD202603080009', 79.80, 'COMPLETED', '杭州市西湖区文一路100号', '13700137006', '周八', '微信支付', '2026-03-08 12:40:00', '2026-03-09 09:50:00'),
(4, 'ORD202603120010', 148.50, 'COMPLETED', '成都市武侯区天府大道200号', '13800138007', '吴九', '银行卡', '2026-03-12 14:15:00', '2026-03-13 11:20:00'),
(8, 'ORD202603180011', 234.60, 'SHIPPED', '南京市鼓楼区中山路50号', '13900139008', '郑十', '微信支付', '2026-03-18 16:30:00', '2026-03-19 10:45:00'),
(11, 'ORD202603250012', 84.00, 'COMPLETED', '武汉市江汉区解放大道300号', '13700137009', '钱一', '支付宝', '2026-03-25 11:20:00', '2026-03-26 15:30:00'),

-- 2026年4月的订单（最近）
(4, 'ORD202604020013', 119.70, 'COMPLETED', '西安市雁塔区高新路88号', '13800138010', '孙二', '微信支付', '2026-04-02 09:30:00', '2026-04-03 14:15:00'),
(8, 'ORD202604050014', 229.20, 'COMPLETED', '重庆市渝中区解放碑步行街', '13900139011', '李三', '支付宝', '2026-04-05 13:45:00', '2026-04-06 10:20:00'),
(11, 'ORD202604080015', 98.00, 'SHIPPED', '天津市和平区南京路100号', '13700137012', '周四', '微信支付', '2026-04-08 15:20:00', '2026-04-09 09:30:00'),
(4, 'ORD202604120016', 159.60, 'SHIPPED', '长沙市岳麓区麓山南路200号', '13800138013', '吴五', '银行卡', '2026-04-12 10:50:00', '2026-04-13 11:40:00'),
(8, 'ORD202604150017', 125.00, 'PAID', '郑州市金水区花园路150号', '13900139014', '郑六', '微信支付', '2026-04-15 14:30:00', '2026-04-15 14:30:00'),
(11, 'ORD202604160018', 278.70, 'PAID', '济南市历下区泉城路88号', '13700137015', '王七', '支付宝', '2026-04-16 16:10:00', '2026-04-16 16:10:00');

-- 插入订单详情（order_item）
-- 订单1的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(1, 3, 3, 39.90, 119.70, '有机白菜', 119.70);

-- 订单2的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(2, 4, 3, 25.00, 75.00, '绿色黄瓜', 75.00);

-- 订单3的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(3, 3, 5, 39.90, 199.50, '有机白菜', 199.50);

-- 订单4的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(4, 9, 9, 9.90, 89.10, '土豆', 89.10);

-- 订单5的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(5, 4, 4, 25.00, 100.00, '绿色黄瓜', 100.00),
(5, 10, 8, 7.00, 56.00, '西红柿', 56.00);

-- 订单6的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(6, 3, 3, 39.90, 119.70, '有机白菜', 119.70),
(6, 9, 10, 9.90, 99.00, '土豆', 99.00);

-- 订单7的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(7, 10, 9, 7.00, 63.00, '西红柿', 63.00);

-- 订单8的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(8, 4, 5, 25.00, 125.00, '绿色黄瓜', 125.00);

-- 订单9的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(9, 3, 2, 39.90, 79.80, '有机白菜', 79.80);

-- 订单10的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(10, 9, 15, 9.90, 148.50, '土豆', 148.50);

-- 订单11的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(11, 3, 4, 39.90, 159.60, '有机白菜', 159.60),
(11, 4, 3, 25.00, 75.00, '绿色黄瓜', 75.00);

-- 订单12的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(12, 10, 12, 7.00, 84.00, '西红柿', 84.00);

-- 订单13的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(13, 3, 3, 39.90, 119.70, '有机白菜', 119.70);

-- 订单14的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(14, 4, 6, 25.00, 150.00, '绿色黄瓜', 150.00),
(14, 9, 8, 9.90, 79.20, '土豆', 79.20);

-- 订单15的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(15, 10, 14, 7.00, 98.00, '西红柿', 98.00);

-- 订单16的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(16, 3, 4, 39.90, 159.60, '有机白菜', 159.60);

-- 订单17的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(17, 4, 5, 25.00, 125.00, '绿色黄瓜', 125.00);

-- 订单18的详情
INSERT INTO order_item (order_id, product_id, quantity, price, subtotal, product_name, total_price) VALUES
(18, 3, 5, 39.90, 199.50, '有机白菜', 199.50),
(18, 9, 8, 9.90, 79.20, '土豆', 79.20);

SELECT '订单数据插入完成！' AS message;
