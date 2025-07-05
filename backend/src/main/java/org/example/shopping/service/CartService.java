package org.example.startup.service;

import org.example.startup.dto.CartItemDto;
import org.example.startup.model.CartItem;
import org.example.startup.model.Product;
import org.example.startup.model.User;
import org.example.startup.repository.CartItemRepository;
import org.example.startup.repository.ProductRepository;
import org.example.startup.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartItemRepository cartItemRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RedisLockService redisLockService;
    
    @Autowired
    private RedissonStockService redissonStockService;
    
    /**
     * 添加商品到购物车
     */
    public CartItemDto addToCart(Long userId, Long productId, Integer quantity) {
        return redisLockService.executeWithLock(productId, 10, () -> {
            // 检查商品是否存在
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("商品不存在"));
            
            // 从Redis检查库存
            Integer currentStock = redissonStockService.getStock(productId);
            if (currentStock == null || currentStock < quantity) {
                throw new RuntimeException("库存不足，当前库存: " + (currentStock != null ? currentStock : 0));
            }
            
            // 检查是否已存在该商品
            Optional<CartItem> existingItem = cartItemRepository.findByUserIdAndProductId(userId, productId);
            
            CartItem cartItem;
            if (existingItem.isPresent()) {
                // 更新数量
                cartItem = existingItem.get();
                int newQuantity = cartItem.getQuantity() + quantity;
                
                // 再次检查库存
                if (currentStock < newQuantity) {
                    throw new RuntimeException("库存不足，当前库存: " + currentStock + 
                            "，购物车已有: " + cartItem.getQuantity());
                }
                
                cartItem.setQuantity(newQuantity);
            } else {
                // 创建新的购物车项
                cartItem = new CartItem(userId, productId, quantity);
            }
            
            cartItem = cartItemRepository.save(cartItem);
            
            // 手动设置product关联，用于DTO转换
            cartItem.setProduct(product);
            
            return new CartItemDto(cartItem);
        });
    }
    
    /**
     * 获取用户购物车
     */
    public List<CartItemDto> getCartItems(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        return cartItems.stream()
                .map(CartItemDto::new)
                .collect(Collectors.toList());
    }
    
    /**
     * 更新购物车商品数量
     */
    public CartItemDto updateCartItemQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new RuntimeException("数量必须大于0");
        }
        
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("购物车中没有该商品"));
        
        // 检查库存
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("商品不存在"));
        
        if (product.getStock() < quantity) {
            throw new RuntimeException("库存不足，当前库存: " + product.getStock());
        }
        
        cartItem.setQuantity(quantity);
        cartItem = cartItemRepository.save(cartItem);
        
        // 手动设置product关联
        cartItem.setProduct(product);
        
        return new CartItemDto(cartItem);
    }
    
    /**
     * 从购物车删除商品
     */
    public void removeFromCart(Long userId, Long productId) {
        CartItem cartItem = cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new RuntimeException("购物车中没有该商品"));
        
        cartItemRepository.delete(cartItem);
    }
    
    /**
     * 清空购物车
     */
    public void clearCart(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }
    
    /**
     * 获取购物车统计信息
     */
    public CartSummary getCartSummary(Long userId) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        
        int totalItems = cartItems.size();
        long totalQuantity = cartItems.stream()
                .mapToLong(CartItem::getQuantity)
                .sum();
        
        BigDecimal totalAmount = cartItems.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        return new CartSummary(totalItems, totalQuantity, totalAmount);
    }
    
    /**
     * 使用余额结账
     */
    public CheckoutResult checkout(Long userId) {
        // 获取用户信息
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 获取购物车商品
        List<CartItem> cartItems = cartItemRepository.findByUserIdWithProduct(userId);
        if (cartItems.isEmpty()) {
            throw new RuntimeException("购物车为空");
        }
        
        // 计算总金额
        BigDecimal totalAmount = cartItems.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getPrice() != null)
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // 检查余额是否足够
        if (user.getBalance().compareTo(totalAmount) < 0) {
            throw new RuntimeException("余额不足，当前余额: " + user.getBalance() + "，需要支付: " + totalAmount);
        }
        
        // 使用分布式锁进行库存扣减，防止超售
        for (CartItem item : cartItems) {
            Long productId = item.getProduct().getId();
            Integer quantity = item.getQuantity();
            
            redisLockService.executeWithLock(productId, 10, () -> {
                // 原子性扣减Redis库存
                Long remainingStock = redissonStockService.deductStock(productId, quantity);
                if (remainingStock == -1) {
                    throw new RuntimeException("商品 " + item.getProduct().getName() + " 库存数据异常");
                } else if (remainingStock == -2) {
                    throw new RuntimeException("商品 " + item.getProduct().getName() + " 库存不足");
                }
                
                // 同步库存到数据库
                redissonStockService.syncStockToDB(productId);
                return null;
            });
        }
        
        // 扣减用户余额
        user.setBalance(user.getBalance().subtract(totalAmount));
        userRepository.save(user);
        
        // 清空购物车
        cartItemRepository.deleteByUserId(userId);
        
        return new CheckoutResult(true, "结账成功", totalAmount, user.getBalance());
    }
    
    /**
     * 购物车摘要信息
     */
    public static class CartSummary {
        private int totalItems;        // 商品种类数
        private long totalQuantity;   // 商品总数量
        private BigDecimal totalAmount; // 总金额
        
        public CartSummary(int totalItems, long totalQuantity, BigDecimal totalAmount) {
            this.totalItems = totalItems;
            this.totalQuantity = totalQuantity;
            this.totalAmount = totalAmount;
        }
        
        // Getters
        public int getTotalItems() { return totalItems; }
        public long getTotalQuantity() { return totalQuantity; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        
        // Setters
        public void setTotalItems(int totalItems) { this.totalItems = totalItems; }
        public void setTotalQuantity(long totalQuantity) { this.totalQuantity = totalQuantity; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    }
    
    /**
     * 结账结果
     */
    public static class CheckoutResult {
        private boolean success;
        private String message;
        private BigDecimal totalAmount;
        private BigDecimal remainingBalance;
        
        public CheckoutResult(boolean success, String message, BigDecimal totalAmount, BigDecimal remainingBalance) {
            this.success = success;
            this.message = message;
            this.totalAmount = totalAmount;
            this.remainingBalance = remainingBalance;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public BigDecimal getRemainingBalance() { return remainingBalance; }
        
        // Setters
        public void setSuccess(boolean success) { this.success = success; }
        public void setMessage(String message) { this.message = message; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        public void setRemainingBalance(BigDecimal remainingBalance) { this.remainingBalance = remainingBalance; }
    }
}