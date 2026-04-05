use farm_market;
-- 用户表
CREATE TABLE `USER` (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(32) NOT NULL,
                        role VARCHAR(20) NOT NULL,
                        nickname VARCHAR(50),
                        phone VARCHAR(20),
                        email VARCHAR(100),
                        address VARCHAR(200),
                        is_verified BOOLEAN DEFAULT FALSE,
                        create_time DATETIME NOT NULL,
                        update_time DATETIME NOT NULL
);

-- 产品表
CREATE TABLE `PRODUCT` (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           farmer_id BIGINT NOT NULL,
                           name VARCHAR(100) NOT NULL,
                           description TEXT,
                           category VARCHAR(50),
                           price DECIMAL(10,2) NOT NULL,
                           stock INT NOT NULL,
                           unit VARCHAR(20),
                           images TEXT,
                           is_on_sale BOOLEAN DEFAULT TRUE,
                           view_count INT DEFAULT 0,
                           sales_count INT DEFAULT 0,
                           create_time DATETIME NOT NULL,
                           update_time DATETIME NOT NULL,
                           FOREIGN KEY (farmer_id) REFERENCES USER(id)
);

-- 订单表
CREATE TABLE `ORDER` (
                         id BIGINT PRIMARY KEY AUTO_INCREMENT,
                         user_id BIGINT NOT NULL,
                         order_no VARCHAR(50) NOT NULL UNIQUE,
                         total_amount DECIMAL(10,2) NOT NULL,
                         status VARCHAR(20) NOT NULL,
                         address VARCHAR(200) NOT NULL,
                         phone VARCHAR(20) NOT NULL,
                         receiver_name VARCHAR(50) NOT NULL,
                         payment_method VARCHAR(20),
                         create_time DATETIME NOT NULL,
                         update_time DATETIME NOT NULL,
                         FOREIGN KEY (user_id) REFERENCES USER(id)
);

-- 订单项表
CREATE TABLE `ORDER_ITEM` (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              order_id BIGINT NOT NULL,
                              product_id BIGINT NOT NULL,
                              quantity INT NOT NULL,
                              price DECIMAL(10,2) NOT NULL,
                              subtotal DECIMAL(10,2) NOT NULL,
                              FOREIGN KEY (order_id) REFERENCES `ORDER`(id),
                              FOREIGN KEY (product_id) REFERENCES PRODUCT(id)
);

-- 购物车项表
CREATE TABLE `CART_ITEM` (
                             id BIGINT PRIMARY KEY AUTO_INCREMENT,
                             user_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity INT NOT NULL,
                             create_time DATETIME NOT NULL,
                             update_time DATETIME NOT NULL,
                             FOREIGN KEY (user_id) REFERENCES USER(id),
                             FOREIGN KEY (product_id) REFERENCES PRODUCT(id)
);

-- 设备表
CREATE TABLE `EQUIPMENT_WYJ` (
                                 id BIGINT PRIMARY KEY,
                                 name VARCHAR(100),
                                 ps VARCHAR(200),
                                 brand VARCHAR(100),
                                 storage_date VARCHAR(50)
);