package org.example.startup.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
public class RedisLockService {

    @Autowired
    private RedissonClient redissonClient;

    private static final String LOCK_PREFIX = "product_lock:";

    /**
     * 尝试获取分布式锁
     * @param productId 商品ID
     * @param timeout 超时时间（秒）
     * @return 锁对象，获取失败返回null
     */
    public RLock tryLock(Long productId, long timeout) {
        String lockKey = LOCK_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(timeout, TimeUnit.SECONDS);
            return acquired ? lock : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 尝试获取分布式锁（带自动续期）
     * @param productId 商品ID
     * @param waitTime 等待时间（秒）
     * @param leaseTime 锁持有时间（秒），-1表示自动续期
     * @return 锁对象，获取失败返回null
     */
    public RLock tryLock(Long productId, long waitTime, long leaseTime) {
        String lockKey = LOCK_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS);
            return acquired ? lock : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    /**
     * 释放分布式锁
     * @param lock 锁对象
     */
    public void releaseLock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 在锁保护下执行操作（自动续期）
     * @param productId 商品ID
     * @param waitTime 等待锁时间（秒）
     * @param leaseTime 锁持有时间（秒），-1表示自动续期
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(Long productId, long waitTime, long leaseTime, Supplier<T> action) {
        String lockKey = LOCK_PREFIX + productId;
        RLock lock = redissonClient.getLock(lockKey);
        
        try {
            if (lock.tryLock(waitTime, leaseTime, TimeUnit.SECONDS)) {
                return action.get();
            } else {
                throw new RuntimeException("获取锁失败，系统繁忙，请稍后重试");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("锁等待被中断");
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    /**
     * 在锁保护下执行操作（兼容原有接口）
     * @param productId 商品ID
     * @param timeout 锁超时时间（秒）
     * @param action 要执行的操作
     * @return 操作结果
     */
    public <T> T executeWithLock(Long productId, long timeout, Supplier<T> action) {
        return executeWithLock(productId, timeout, -1, action);
    }
}