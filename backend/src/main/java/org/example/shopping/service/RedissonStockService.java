package org.example.startup.service;

import org.example.startup.model.Product;
import org.example.startup.repository.ProductRepository;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBatch;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedissonStockService {

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private ProductRepository productRepository;

    private static final String STOCK_PREFIX = "stock:";
    private static final long STOCK_CACHE_EXPIRE = 3600; // 1小时过期

    /**
     * 初始化商品库存到Redis
     * @param productId 商品ID
     * @param stock 库存数量
     */
    public void initStock(Long productId, Integer stock) {
        String stockKey = STOCK_PREFIX + productId;
        RAtomicLong atomicStock = redissonClient.getAtomicLong(stockKey);
        atomicStock.set(stock);
        atomicStock.expire(STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 从数据库同步库存到Redis
     * @param productId 商品ID
     */
    public void syncStockFromDB(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        initStock(productId, product.getStock());
    }

    /**
     * 获取商品库存
     * @param productId 商品ID
     * @return 库存数量，不存在返回null
     */
    public Integer getStock(Long productId) {
        String stockKey = STOCK_PREFIX + productId;
        RAtomicLong atomicStock = redissonClient.getAtomicLong(stockKey);
        
        if (!atomicStock.isExists()) {
            // 如果Redis中不存在，尝试从数据库同步
            try {
                syncStockFromDB(productId);
                return (int) atomicStock.get();
            } catch (Exception e) {
                return null;
            }
        }
        
        return (int) atomicStock.get();
    }

    /**
     * 原子性扣减库存
     * @param productId 商品ID
     * @param quantity 扣减数量
     * @return 扣减后的库存数量，-1表示库存不存在，-2表示库存不足
     */
    public Long deductStock(Long productId, Integer quantity) {
        String stockKey = STOCK_PREFIX + productId;
        RAtomicLong atomicStock = redissonClient.getAtomicLong(stockKey);
        
        // 检查库存是否存在
        if (!atomicStock.isExists()) {
            return -1L;
        }
        
        // 原子性检查并扣减
        long currentStock = atomicStock.get();
        if (currentStock < quantity) {
            return -2L;
        }
        
        long remaining = atomicStock.addAndGet(-quantity);
        if (remaining < 0) {
            // 如果扣减后为负数，回滚
            atomicStock.addAndGet(quantity);
            return -2L;
        }
        
        // 重新设置过期时间
        atomicStock.expire(STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
        return remaining;
    }

    /**
     * 原子性增加库存
     * @param productId 商品ID
     * @param quantity 增加数量
     * @return 增加后的库存数量
     */
    public Long addStock(Long productId, Integer quantity) {
        String stockKey = STOCK_PREFIX + productId;
        RAtomicLong atomicStock = redissonClient.getAtomicLong(stockKey);
        
        long newStock = atomicStock.addAndGet(quantity);
        atomicStock.expire(STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
        return newStock;
    }

    /**
     * 将Redis中的库存同步到数据库
     * @param productId 商品ID
     */
    public void syncStockToDB(Long productId) {
        Integer redisStock = getStock(productId);
        if (redisStock == null) {
            return;
        }
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        product.setStock(redisStock);
        productRepository.save(product);
    }

    /**
     * 删除Redis中的库存缓存
     * @param productId 商品ID
     */
    public void deleteStock(Long productId) {
        String stockKey = STOCK_PREFIX + productId;
        RAtomicLong atomicStock = redissonClient.getAtomicLong(stockKey);
        atomicStock.delete();
    }

    /**
     * 批量初始化库存
     * @param products 商品列表
     */
    public void batchInitStock(List<Product> products) {
        RBatch batch = redissonClient.createBatch();
        
        for (Product product : products) {
            String stockKey = STOCK_PREFIX + product.getId();
            batch.getAtomicLong(stockKey).setAsync(product.getStock());
            batch.getAtomicLong(stockKey).expireAsync(STOCK_CACHE_EXPIRE, TimeUnit.SECONDS);
        }
        
        batch.execute();
    }

    /**
     * 检查库存是否充足
     * @param productId 商品ID
     * @param quantity 需要的数量
     * @return 是否充足
     */
    public boolean isStockSufficient(Long productId, Integer quantity) {
        Integer currentStock = getStock(productId);
        return currentStock != null && currentStock >= quantity;
    }
}