# 基于阿里云微服务生态的轻量级电商系统

这是一个面向毕业设计的轻量级电商微服务项目，采用 `Spring Boot + Spring Cloud Alibaba + Nacos + Sentinel + Seata/RocketMQ` 作为后端主线，前端包含商城用户端和管理后台。

## 当前工程结构

- `backend/common`：统一响应、ID 生成等公共能力
- `backend/gateway-service`：统一 API 网关、路由、简单 Token 前置校验
- `backend/auth-user-service`：注册、登录、JWT、用户角色
- `backend/product-service`：分类、商品、上下架、库存扣减
- `backend/trade-service`：购物车、订单、模拟支付
- `backend/search-service`：商品搜索、排序、同类推荐
- `backend/message-service`：公告、站内消息、已读状态
- `frontend/shop-web`：商城用户端
- `frontend/admin-web`：管理后台
- `infra/mysql/init`：逻辑数据库初始化脚本
- `docker-compose.yml`：基础设施、后端服务和前端应用编排

## 本地后端验证

```bash
mvn test
```

## 后端打包

```bash
mvn -DskipTests package
```

## 前端验证与构建

```bash
npm install
npm test
npm run build
```

## 前端本地启动

```bash
npm run dev -w shop-web
npm run dev -w admin-web
```

默认开发地址：

- 商城用户端：`http://localhost:5175`
- 管理后台：`http://localhost:5174`

## Docker Compose 启动

```bash
docker compose up --build
```

启动后主要入口：

- 网关：`http://localhost:8080`
- 商城用户端：`http://localhost:3000`
- 管理后台：`http://localhost:3001`
- Nacos：`http://localhost:8848/nacos`
- Sentinel：`http://localhost:8858`
- Seata 控制台：`http://localhost:7091`

## 典型接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/products`
- `POST /api/trade/cart/items`
- `POST /api/trade/orders`
- `POST /api/trade/orders/{orderId}/pay`
- `GET /api/search/products`
- `GET /api/messages/notices`
