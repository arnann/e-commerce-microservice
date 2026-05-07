SET NAMES utf8mb4;

USE product_db;

UPDATE product_category
SET name = CASE id
    WHEN 10 THEN '数码配件'
    WHEN 11 THEN '生活用品'
    WHEN 12 THEN '图书资料'
    ELSE name
END
WHERE id IN (10, 11, 12);

UPDATE product_spu
SET
    name = CASE id
        WHEN 101 THEN '无线降噪耳机'
        WHEN 102 THEN '机械键盘'
        WHEN 103 THEN '运动水杯'
        WHEN 104 THEN '微服务实践手册'
        WHEN 105 THEN '无线鼠标'
        ELSE name
    END,
    description = CASE id
        WHEN 101 THEN '通勤、学习和运动都能稳定使用的蓝牙耳机。'
        WHEN 102 THEN '热插拔轴体，适合代码、论文和日常办公。'
        WHEN 103 THEN '大容量防漏杯，轻便耐用。'
        WHEN 104 THEN '覆盖注册发现、网关、限流、消息队列和部署。'
        WHEN 105 THEN '轻量低延迟，适合办公和学习。'
        ELSE description
    END
WHERE id IN (101, 102, 103, 104, 105);

USE trade_db;

UPDATE order_item
SET product_name = CASE
    WHEN order_id = 9001 AND product_id = 101 THEN '无线降噪耳机'
    WHEN order_id = 9002 AND product_id = 102 THEN '机械键盘'
    WHEN order_id = 9003 AND product_id = 103 THEN '运动水杯'
    WHEN order_id = 9004 AND product_id = 104 THEN '微服务实践手册'
    ELSE product_name
END
WHERE (order_id = 9001 AND product_id = 101)
   OR (order_id = 9002 AND product_id = 102)
   OR (order_id = 9003 AND product_id = 103)
   OR (order_id = 9004 AND product_id = 104);

UPDATE cart_item
SET product_name = CASE product_id
    WHEN 101 THEN '无线降噪耳机'
    WHEN 102 THEN '机械键盘'
    WHEN 103 THEN '运动水杯'
    WHEN 104 THEN '微服务实践手册'
    WHEN 105 THEN '无线鼠标'
    ELSE product_name
END
WHERE product_id IN (101, 102, 103, 104, 105);

USE message_db;

UPDATE notice
SET
    title = CASE id
        WHEN 1 THEN '五一活动'
        WHEN 2 THEN '系统维护'
        WHEN 3 THEN '支付通知'
        ELSE title
    END,
    content = CASE id
        WHEN 1 THEN '全场商品满减已开启'
        WHEN 2 THEN '23:00 到 23:30 进行维护'
        WHEN 3 THEN '订单支付成功后将自动发送站内信'
        ELSE content
    END
WHERE id IN (1, 2, 3);

UPDATE user_message
SET content = CASE id
    WHEN 5001 THEN '订单 #9001 已支付成功'
    WHEN 5002 THEN '订单 #9003 正在备货'
    WHEN 5003 THEN '订单 #9002 待支付'
    ELSE content
END
WHERE id IN (5001, 5002, 5003);
