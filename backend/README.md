# Shopping Web Backend

基于Spring Boot的电商网站后端服务，提供完整的商品管理、用户认证、订单处理和防超售功能。

## 核心功能

### 1. 用户认证与授权
- 基于Spring Security的认证系统
- JWT令牌支持
- 用户角色权限管理

### 2. 商品管理
- 商品CRUD操作
- 商品分类管理
- 商品库存管理
- 商品搜索功能

### 3. 订单系统
- 订单创建和管理
- 订单状态追踪
- 支付集成接口

### 4. 防超售系统
- 基于Redisson的分布式锁
- Redis缓存优化
- 原子性库存操作
- 高并发支持

### 5. 分布式功能
- 分布式会话管理
- 分布式缓存
- 分布式锁

## 技术栈

- Spring Boot 3.4.5
- Spring Security
- Spring Data JPA
- MySQL
- Redis + Redisson
- JWT

## 系统要求

- JDK 17+
- MySQL 8.0+
- Redis 6.0+

## 配置说明

### Redis配置
- 服务地址：127.0.0.1:6379
- 数据库：0
- 连接池：最小空闲连接10个，连接池大小64个
- 超时设置：连接超时10000ms，命令超时3000ms

### 数据库配置
请在`application.properties`中配置数据库连接信息：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/shopping
spring.datasource.username=your_username
spring.datasource.password=your_password
```

## API文档

### 用户认证
```http
POST /api/auth/login
POST /api/auth/register
POST /api/auth/refresh-token
```

### 商品管理
```http
GET /api/products
GET /api/products/{id}
POST /api/products
PUT /api/products/{id}
DELETE /api/products/{id}
```

### 购物车
```http
GET /api/cart
POST /api/cart/add
POST /api/cart/checkout
DELETE /api/cart/{productId}
```

### 库存管理
```http
GET /api/stock/{productId}
POST /api/stock/sync/{productId}
POST /api/stock/sync-to-db/{productId}
POST /api/stock/set/{productId}
```


## 性能优化

1. Redis缓存策略
   - 商品信息缓存
   - 库存数据缓存
   - 用户会话缓存

2. 数据库优化
   - 索引优化
   - 连接池配置
   - 事务管理

3. 并发控制
   - 分布式锁
   - 库存原子操作
   - 事务隔离级别

