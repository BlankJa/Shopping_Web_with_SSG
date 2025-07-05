# Redisson防超售功能集成指南

本项目已成功集成Redisson服务来防止商品超售问题。Redisson是一个功能强大的Redis Java客户端，提供了更可靠的分布式锁和原子操作。以下是详细的功能说明和使用指南。

## 系统架构

### Redisson配置
- **服务地址**：127.0.0.1:6379
- **数据库**：0
- **连接池配置**：最小空闲连接10个，连接池大小64个
- **超时设置**：连接超时10000ms，命令超时3000ms
- **密码认证**：支持Redis密码认证

### 防超售机制

#### 1. 分布式锁（RedisLockService）
- 使用Redisson的RLock实现分布式锁，确保同一商品的库存操作串行化
- 锁的超时时间为10秒，支持自动续期机制，防止死锁
- 支持可重入锁，同一线程可多次获取同一把锁
- 内置看门狗机制，自动延长锁的有效期
- 更可靠的锁释放机制，避免误释放其他线程的锁

#### 2. 原子性库存操作（RedissonStockService）
- 使用Redisson的RAtomicLong实现原子性的库存扣减和增加
- 库存数据缓存在Redis中，提高访问速度
- 支持库存数据在Redis和数据库之间的双向同步
- 库存缓存过期时间为1小时
- 支持批量操作（RBatch），提高批量初始化性能

## 核心功能

### 1. 库存管理API

#### 获取商品库存
```http
GET /api/stock/{productId}
```

**响应示例：**
```json
{
  "productId": 1,
  "stock": 100
}
```

#### 同步数据库库存到Redis
```http
POST /api/stock/sync/{productId}
```

#### 同步Redis库存到数据库
```http
POST /api/stock/sync-to-db/{productId}
```

#### 手动设置商品库存
```http
POST /api/stock/set/{productId}
Content-Type: application/json

{
  "stock": 100
}
```

### 2. 购物车防超售功能

#### 添加商品到购物车（已升级）
```http
POST /api/cart/add
Content-Type: application/json
Authorization: Bearer {jwt_token}

{
  "productId": 1,
  "quantity": 2
}
```

**防超售特性：**
- 使用分布式锁确保库存检查和扣减的原子性
- 从Redis实时获取库存信息
- 支持高并发场景下的库存控制

#### 结账功能（已升级）
```http
POST /api/cart/checkout
Authorization: Bearer {jwt_token}
```

**防超售特性：**
- 对每个商品使用分布式锁
- 使用Lua脚本原子性扣减库存
- 扣减成功后同步到数据库
- 支持事务回滚机制

### 3. 测试功能

#### 并发购买测试
```http
POST /api/test/concurrent-purchase
Content-Type: application/json

{
  "productId": 1,
  "quantity": 1,
  "concurrentUsers": 10,
  "userId": 1
}
```

**测试结果示例：**
```json
{
  "message": "并发购买测试完成",
  "productId": 1,
  "initialStock": 10,
  "finalStock": 0,
  "concurrentUsers": 10,
  "successCount": 10,
  "failCount": 0,
  "expectedFinalStock": 0
}
```

## 技术实现细节

### 1. 分布式锁实现
```java
// 获取锁（支持自动续期）
String lockValue = redisLockService.tryLock(productId, 10);

// 执行业务逻辑（推荐方式）
redisLockService.executeWithLock(productId, 10, () -> {
    // 业务代码
    return result;
});

// 手动管理锁（高级用法）
RLock lock = redissonClient.getLock("lock:product:" + productId);
try {
    if (lock.tryLock(10, TimeUnit.SECONDS)) {
        // 业务逻辑
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

### 2. 原子性库存操作
```java
// 扣减库存（使用RAtomicLong）
Long remainingStock = redissonStockService.deductStock(productId, quantity);
if (remainingStock == -2) {
    throw new RuntimeException("库存不足");
}

// 增加库存（回滚）
redissonStockService.addStock(productId, quantity);

// 批量初始化库存（使用RBatch）
redissonStockService.batchInitStock(productList);
```

### 3. 库存同步机制
- **应用启动时**：自动同步所有商品库存到Redis
- **商品保存时**：自动同步库存到Redis
- **商品删除时**：清理Redis中的库存缓存
- **结账成功后**：同步Redis库存到数据库

## 使用流程

### 1. 启动应用
1. 确保Redis服务运行在127.0.0.1:6379
2. 启动Spring Boot应用
3. 应用会自动同步数据库库存到Redis

### 2. 测试防超售功能
1. 使用测试接口设置商品库存
2. 使用并发测试接口验证防超售效果
3. 观察测试结果中的成功和失败次数

### 3. 正常使用
1. 用户添加商品到购物车（自动防超售）
2. 用户结账（原子性扣减库存）
3. 系统自动同步库存到数据库

## 性能优化

1. **缓存策略**：库存数据缓存1小时，减少数据库访问
2. **连接池**：配置Redisson连接池，支持高并发访问
3. **原子操作**：使用RAtomicLong确保操作的原子性，性能优于Lua脚本
4. **细粒度锁**：只锁定特定商品，提高并发性能
5. **批量操作**：使用RBatch进行批量库存初始化，减少网络往返
6. **自动续期**：分布式锁支持自动续期，避免业务执行时间过长导致的锁失效

## 错误处理

- **库存不足**：返回明确的错误信息和当前库存
- **系统繁忙**：分布式锁获取失败时的友好提示
- **数据异常**：库存数据不存在时的自动恢复机制

## 监控建议

1. 监控Redisson连接状态和性能指标
2. 监控分布式锁的获取成功率、等待时间和自动续期情况
3. 监控库存同步的成功率
4. 设置库存不足的告警机制
5. 定期检查Redis和数据库库存数据的一致性
6. 监控RAtomicLong操作的性能指标
7. 关注批量操作的执行效率

## 部署注意事项

1. 确保Redis服务的高可用性
2. 配置合适的Redis内存和持久化策略
3. 监控Redis的内存使用情况
4. 定期备份Redis数据
5. 在生产环境中调整锁的超时时间和重试策略
6. 配置Redisson的连接池参数以适应业务负载
7. 考虑使用Redis集群模式以提高可用性和性能
8. 合理设置看门狗的续期时间间隔