# 基于阿里云微服务生态的轻量级电商系统

这是一个面向毕业设计/课程设计的轻量级电商微服务项目，采用 `Spring Boot + Spring Cloud Alibaba + Nacos + Sentinel + Seata + RocketMQ` 作为后端主线，前端包含商城用户端和管理后台。

项目已经接入真实 MySQL 数据库，不再依赖前端写死数据。商城端支持注册、登录、商品浏览、购物车、下单、支付和消息；管理端支持商品、分类、订单、用户、公告和服务监控管理。

## 工程结构

- `backend/common`：统一响应、ID 生成等公共能力
- `backend/gateway-service`：统一 API 网关、路由、简单 Token 前置校验
- `backend/auth-user-service`：注册、登录、JWT、用户角色、用户状态、后台用户管理
- `backend/product-service`：分类、商品、上下架、库存扣减
- `backend/trade-service`：购物车、订单、模拟支付、发货
- `backend/search-service`：商品搜索、排序、同类推荐
- `backend/message-service`：公告、站内消息、已读状态
- `frontend/shop-web`：商城用户端
- `frontend/admin-web`：管理后台
- `infra/mysql/init`：MySQL 初始化脚本
- `docker-compose.yml`：基础设施、后端服务和前端应用编排

## 环境要求

推荐直接使用 Docker Compose 启动完整环境。

- Docker Desktop 或 Docker Engine
- Docker Compose v2
- 可用内存建议 6GB 以上
- 本地端口不要被占用：`3000`、`3001`、`3306`、`6379`、`8080`、`8101`-`8105`、`8848`、`8858`、`9876`、`10911`、`7091`、`8091`

本地单独跑测试/前端时还需要：

- JDK 17
- Maven 3.9+
- Node.js 22+ / npm

## 一键启动

在项目根目录执行：

```bash
docker compose up -d --build
```

首次构建会拉取 MySQL、Nacos、RocketMQ、Seata、Sentinel、JDK、Node、Nginx 等镜像，并编译多个后端服务和前端应用，耗时会比较久。后续只改某个服务时，建议只重建对应服务，例如：

```bash
docker compose up -d --build auth-user-service
docker compose up -d --build trade-service
docker compose up -d --build admin-web
docker compose up -d --build shop-web
```

如果你在 Windows + WSL 下运行，并且普通 WSL 用户没有 Docker socket 权限，可以在 WSL root 下执行：

```bash
cd /path/to/e-commerce-microservice
docker compose up -d --build
```

或者从 PowerShell 里执行：

```powershell
wsl -u root -e sh -lc 'cd /path/to/e-commerce-microservice && docker compose up -d --build'
```

## 启动后入口

- 商城用户端：`http://localhost:3000`
- 管理后台：`http://localhost:3001`
- API 网关：`http://localhost:8080`
- Nacos：`http://localhost:8848/nacos`
- Sentinel：`http://localhost:8858`
- Seata 控制台：`http://localhost:7091`
- MySQL：`localhost:3306`

## 演示账号

所有种子账号初始密码都是：

```text
demo123
```

| 端 | 邮箱 | 角色 | 说明 |
| --- | --- | --- | --- |
| 商城端 | `alice@example.com` | 普通用户 | 有历史订单和站内消息 |
| 商城端 | `ming@example.com` | 普通用户 | 有历史订单 |
| 管理端 | `admin@example.com` | 管理员 | 可进入后台管理 |

商城端也支持直接注册新用户。管理后台的“用户管理”支持新增用户、编辑昵称、修改角色、启用/停用用户和重置密码。

## 数据库初始化说明

数据库初始化脚本在：

```text
infra/mysql/init/01-schema.sql
```

`docker-compose.yml` 会把该目录挂载到 MySQL 官方镜像的初始化目录：

```yaml
mysql-data:/var/lib/mysql
./infra/mysql/init:/docker-entrypoint-initdb.d
```

因此，**第一次启动 MySQL 容器且 MySQL 数据目录为空时，脚本会自动执行**。脚本会创建并初始化：

- `auth_user_db`
- `product_db`
- `trade_db`
- `search_db`
- `message_db`

同时会插入基础数据：

- Alice / Ming / Admin 三个账号
- 商品分类和商品
- 订单、订单明细、购物车表
- 公告和站内消息
- Seata AT 模式需要的 `undo_log` 表

需要注意：MySQL 官方镜像的 `/docker-entrypoint-initdb.d` 只在数据目录为空时执行。也就是说：

- 全新启动：会自动初始化 DB
- 已经启动过并保留了 MySQL volume：不会重复执行初始化脚本
- 修改了 `01-schema.sql` 后想重新初始化：需要删除旧数据卷

重置数据库并重新初始化：

```bash
docker compose down -v
docker compose up -d --build
```

`docker compose down -v` 会删除数据库数据，请只在确认不需要保留当前数据时执行。

如果只是已有库缺少新增字段，不想清库，可以手动执行对应 SQL。例如本项目用户状态字段为：

```bash
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "ALTER TABLE auth_user_db.user_account ADD COLUMN status_code VARCHAR(30) NOT NULL DEFAULT 'NORMAL' AFTER role_code;"
```

全新环境不需要手动执行这类 SQL，初始化脚本会自动建好。

## 验证服务是否启动成功

查看容器状态：

```bash
docker compose ps
```

所有核心服务应处于 `Up` 状态，尤其是：

- `ecommerce-mysql`
- `ecommerce-nacos`
- `ecommerce-seata`
- `ecommerce-gateway`
- `ecommerce-auth-user`
- `ecommerce-product`
- `ecommerce-trade`
- `ecommerce-message`
- `ecommerce-shop-web`
- `ecommerce-admin-web`

接口冒烟：

```bash
curl http://localhost:8080/api/auth/health
curl http://localhost:8080/api/products
curl http://localhost:8080/api/trade/orders
curl http://localhost:8080/api/messages/notices/all
```

也可以只看 HTTP 状态码：

```bash
for url in \
  http://localhost:3000 \
  http://localhost:3001 \
  http://localhost:8080/api/auth/health \
  http://localhost:8080/api/products \
  http://localhost:8080/api/trade/orders \
  http://localhost:8080/api/messages/notices/all
do
  printf "%s -> " "$url"
  curl -sS -o /dev/null -w "%{http_code}\n" --max-time 15 "$url"
done
```

## Seata 说明

本项目的 Seata 不是摆设。`product-service` 和 `trade-service` 都接入了 Seata：

- `trade-service` 下单使用全局事务
- `trade-service` 调用 `product-service` 预占库存
- XID 会向下游服务传递
- `product_db` 和 `trade_db` 都包含 `undo_log` 表
- Seata server 需要保持启动

启动后可以通过日志确认服务注册：

```bash
docker logs ecommerce-trade --tail 120
docker logs ecommerce-product --tail 120
```

正常情况下可以看到 TM/RM 注册、AT datasource proxy 等日志。

## 本地开发命令

后端全量测试：

```bash
mvn test
```

后端指定服务测试：

```bash
mvn -pl backend/auth-user-service -am test
mvn -pl backend/trade-service -am test
```

后端打包：

```bash
mvn -DskipTests package
```

前端安装依赖：

```bash
npm install
```

前端测试和构建：

```bash
npm test
npm run build
```

前端本地开发：

```bash
npm run dev -w shop-web
npm run dev -w admin-web
```

默认开发地址：

- 商城用户端：`http://localhost:5175`
- 管理后台：`http://localhost:5174`

前端本地开发时仍需要后端网关和各微服务运行。最省事的方式是先启动 Docker Compose，再单独启动前端 dev server。

## 常用接口

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/users`
- `POST /api/auth/users`
- `PUT /api/auth/users/{userId}`
- `POST /api/auth/users/{userId}/reset-password`
- `GET /api/products`
- `GET /api/products/categories`
- `POST /api/trade/cart/items`
- `POST /api/trade/orders`
- `POST /api/trade/orders/{orderId}/pay`
- `POST /api/trade/orders/{orderId}/ship`
- `GET /api/messages/notices`
- `GET /api/messages/notices/all`

## 常见问题

### 页面打开了但接口 502

前端 Nginx 会代理 `/api` 到 `gateway-service:8080`。如果网关刚重启，稍等几秒刷新页面。仍不正常时检查：

```bash
docker compose ps gateway-service
docker logs ecommerce-gateway --tail 120
```

### 登录失败

确认 `auth-user-service` 正常：

```bash
curl http://localhost:8080/api/auth/health
```

确认种子账号存在：

```bash
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "SELECT id,email,nickname,role_code,status_code,deleted FROM auth_user_db.user_account ORDER BY id;"
```

### 数据没有初始化

通常是因为 MySQL 数据卷已经存在，初始化脚本不会重复执行。确认当前库：

```bash
docker exec ecommerce-mysql mysql -uroot -proot123456 -e "SHOW DATABASES;"
```

如果要重新初始化，执行：

```bash
docker compose down -v
docker compose up -d --build
```

### 重建太慢

优先只重建改动的服务：

```bash
docker compose up -d --build auth-user-service
docker compose up -d --build admin-web
```

后端 Dockerfile 已启用 BuildKit Maven 缓存层：

```dockerfile
RUN --mount=type=cache,target=/root/.m2 mvn -q -DskipTests -pl backend/${SERVICE_NAME} -am package
```

确保 Docker BuildKit 开启时，后续构建会快很多。
