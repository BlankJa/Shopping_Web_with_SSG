package org.example.startup.repository;

import org.example.startup.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    
    /**
     * 根据用户ID查找购物车商品
     */
    @Query("SELECT c FROM CartItem c LEFT JOIN FETCH c.product WHERE c.userId = :userId ORDER BY c.createdAt DESC")
    List<CartItem> findByUserIdWithProduct(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和商品ID查找购物车项
     */
    Optional<CartItem> findByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 根据用户ID删除所有购物车项
     */
    void deleteByUserId(Long userId);
    
    /**
     * 根据用户ID和商品ID删除购物车项
     */
    void deleteByUserIdAndProductId(Long userId, Long productId);
    
    /**
     * 统计用户购物车中的商品数量
     */
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.userId = :userId")
    Long countByUserId(@Param("userId") Long userId);
    
    /**
     * 计算用户购物车中商品的总数量
     */
    @Query("SELECT COALESCE(SUM(c.quantity), 0) FROM CartItem c WHERE c.userId = :userId")
    Long sumQuantityByUserId(@Param("userId") Long userId);
    
    /**
     * 检查用户是否已将某商品加入购物车
     */
    boolean existsByUserIdAndProductId(Long userId, Long productId);
}