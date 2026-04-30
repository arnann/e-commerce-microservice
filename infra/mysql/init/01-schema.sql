CREATE DATABASE IF NOT EXISTS auth_user_db DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS product_db DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS trade_db DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS search_db DEFAULT CHARACTER SET utf8mb4;
CREATE DATABASE IF NOT EXISTS message_db DEFAULT CHARACTER SET utf8mb4;

USE auth_user_db;
CREATE TABLE IF NOT EXISTS user_account (
    id BIGINT PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    nickname VARCHAR(80) NOT NULL,
    password_hash VARCHAR(120) NOT NULL,
    role_code VARCHAR(40) NOT NULL,
    status_code VARCHAR(30) NOT NULL DEFAULT 'NORMAL',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO user_account (id, email, nickname, password_hash, role_code, status_code)
VALUES
    (1, 'alice@example.com', 'Alice', '$2a$10$GiSm3ckUDsVPvcjULVXSiOfYu3z11EnKch.sXs6hF7dAdKFAhQvt2', 'CUSTOMER', 'NORMAL'),
    (2, 'ming@example.com', 'Ming', '$2a$10$GiSm3ckUDsVPvcjULVXSiOfYu3z11EnKch.sXs6hF7dAdKFAhQvt2', 'CUSTOMER', 'NORMAL'),
    (3, 'admin@example.com', 'Admin', '$2a$10$GiSm3ckUDsVPvcjULVXSiOfYu3z11EnKch.sXs6hF7dAdKFAhQvt2', 'ADMIN', 'NORMAL')
ON DUPLICATE KEY UPDATE
    nickname = VALUES(nickname),
    password_hash = VALUES(password_hash),
    role_code = VALUES(role_code),
    status_code = VALUES(status_code),
    deleted = 0;

USE product_db;
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_category (
    id BIGINT PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    sort_order INT NOT NULL DEFAULT 1,
    status VARCHAR(30) NOT NULL DEFAULT 'ENABLED',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS product_spu (
    id BIGINT PRIMARY KEY,
    category_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    price DECIMAL(12, 2) NOT NULL,
    stock INT NOT NULL,
    status VARCHAR(30) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

INSERT INTO product_category (id, name, sort_order, status)
VALUES
    (10, '数码配件', 1, 'ENABLED'),
    (11, '生活用品', 2, 'ENABLED'),
    (12, '图书资料', 3, 'ENABLED')
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    sort_order = VALUES(sort_order),
    status = VALUES(status),
    deleted = 0;

INSERT INTO product_spu (id, category_id, name, description, price, stock, status)
VALUES
    (101, 10, '无线降噪耳机', '通勤、学习和运动都能稳定使用的蓝牙耳机。', 299.00, 36, 'ON_SALE'),
    (102, 10, '机械键盘', '热插拔轴体，适合代码、论文和日常办公。', 399.00, 18, 'ON_SALE'),
    (103, 11, '运动水杯', '大容量防漏杯，轻便耐用。', 59.00, 90, 'ON_SALE'),
    (104, 12, '微服务实践手册', '覆盖注册发现、网关、限流、消息队列和部署。', 88.00, 42, 'ON_SALE'),
    (105, 10, '无线鼠标', '轻量低延迟，适合办公和学习。', 129.00, 15, 'DRAFT')
ON DUPLICATE KEY UPDATE
    category_id = VALUES(category_id),
    name = VALUES(name),
    description = VALUES(description),
    price = VALUES(price),
    stock = VALUES(stock),
    status = VALUES(status),
    deleted = 0;

USE trade_db;
CREATE TABLE IF NOT EXISTS undo_log (
    branch_id BIGINT NOT NULL COMMENT 'branch transaction id',
    xid VARCHAR(128) NOT NULL COMMENT 'global transaction id',
    context VARCHAR(128) NOT NULL COMMENT 'undo_log context',
    rollback_info LONGBLOB NOT NULL COMMENT 'rollback info',
    log_status INT NOT NULL COMMENT '0:normal status,1:defense status',
    log_created DATETIME(6) NOT NULL COMMENT 'create datetime',
    log_modified DATETIME(6) NOT NULL COMMENT 'modify datetime',
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_info (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_amount DECIMAL(12, 2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS order_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_order_item_order_id (order_id)
);

CREATE TABLE IF NOT EXISTS cart_item (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(120) NOT NULL,
    price DECIMAL(12, 2) NOT NULL,
    quantity INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_cart_user_product (user_id, product_id)
);

INSERT INTO order_info (id, user_id, total_amount, status)
VALUES
    (9001, 1, 598.00, 'PAID'),
    (9002, 2, 399.00, 'PENDING_PAYMENT'),
    (9003, 1, 59.00, 'PAID'),
    (9004, 2, 88.00, 'SHIPPED')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    total_amount = VALUES(total_amount),
    status = VALUES(status),
    deleted = 0;

INSERT INTO order_item (order_id, product_id, product_name, price, quantity, amount)
SELECT 9001, 101, '无线降噪耳机', 299.00, 2, 598.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 9001 AND product_id = 101 AND deleted = 0);
INSERT INTO order_item (order_id, product_id, product_name, price, quantity, amount)
SELECT 9002, 102, '机械键盘', 399.00, 1, 399.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 9002 AND product_id = 102 AND deleted = 0);
INSERT INTO order_item (order_id, product_id, product_name, price, quantity, amount)
SELECT 9003, 103, '运动水杯', 59.00, 1, 59.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 9003 AND product_id = 103 AND deleted = 0);
INSERT INTO order_item (order_id, product_id, product_name, price, quantity, amount)
SELECT 9004, 104, '微服务实践手册', 88.00, 1, 88.00
WHERE NOT EXISTS (SELECT 1 FROM order_item WHERE order_id = 9004 AND product_id = 104 AND deleted = 0);

UPDATE order_item SET product_name = '无线降噪耳机' WHERE order_id = 9001 AND product_id = 101;
UPDATE order_item SET product_name = '机械键盘' WHERE order_id = 9002 AND product_id = 102;
UPDATE order_item SET product_name = '运动水杯' WHERE order_id = 9003 AND product_id = 103;
UPDATE order_item SET product_name = '微服务实践手册' WHERE order_id = 9004 AND product_id = 104;

USE message_db;
CREATE TABLE IF NOT EXISTS notice (
    id BIGINT PRIMARY KEY,
    title VARCHAR(120) NOT NULL,
    content TEXT NOT NULL,
    publisher_id BIGINT,
    published TINYINT NOT NULL DEFAULT 1,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS user_message (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    order_id BIGINT,
    content VARCHAR(500) NOT NULL,
    read_flag TINYINT NOT NULL DEFAULT 0,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted TINYINT NOT NULL DEFAULT 0,
    INDEX idx_user_message_user_id (user_id)
);

INSERT INTO notice (id, title, content, publisher_id, published)
VALUES
    (1, '五一活动', '全场商品满减已开启', 3, 1),
    (2, '系统维护', '23:00 到 23:30 进行维护', 3, 1),
    (3, '支付通知', '订单支付成功后将自动发送站内信', 3, 1)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    publisher_id = VALUES(publisher_id),
    published = VALUES(published),
    deleted = 0;

INSERT INTO user_message (id, user_id, order_id, content, read_flag)
VALUES
    (5001, 1, 9001, '订单 #9001 已支付成功', 0),
    (5002, 1, 9003, '订单 #9003 正在备货', 0),
    (5003, 2, 9002, '订单 #9002 待支付', 0)
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    read_flag = VALUES(read_flag),
    deleted = 0;
