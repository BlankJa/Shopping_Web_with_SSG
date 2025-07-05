package org.example.startup.controller;

import org.example.startup.dto.CartItemDto;
import org.example.startup.service.CartService;
import org.example.startup.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {
    
    @Autowired
    private CartService cartService;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    /**
     * 添加商品到购物车
     */
    @PostMapping("/add")
    public ResponseEntity<?> addToCart(
            @RequestBody AddToCartRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            CartItemDto cartItem = cartService.addToCart(userId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取用户购物车
     */
    @GetMapping
    public ResponseEntity<?> getCartItems(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            List<CartItemDto> cartItems = cartService.getCartItems(userId);
            return ResponseEntity.ok(cartItems);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 更新购物车商品数量
     */
    @PutMapping("/update")
    public ResponseEntity<?> updateCartItem(
            @RequestBody UpdateCartRequest request,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            CartItemDto cartItem = cartService.updateCartItemQuantity(
                    userId, request.getProductId(), request.getQuantity());
            return ResponseEntity.ok(cartItem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 从购物车删除商品
     */
    @DeleteMapping("/remove/{productId}")
    public ResponseEntity<?> removeFromCart(
            @PathVariable Long productId,
            HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            cartService.removeFromCart(userId, productId);
            return ResponseEntity.ok(Map.of("message", "商品已从购物车删除"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 清空购物车
     */
    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            cartService.clearCart(userId);
            return ResponseEntity.ok(Map.of("message", "购物车已清空"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 获取购物车统计信息
     */
    @GetMapping("/summary")
    public ResponseEntity<?> getCartSummary(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            CartService.CartSummary summary = cartService.getCartSummary(userId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 使用余额结账
     */
    @PostMapping("/checkout")
    public ResponseEntity<?> checkout(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            CartService.CheckoutResult result = cartService.checkout(userId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 从Spring Security上下文中获取用户ID（临时调试版本）
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        // 优先从Spring Security的Authentication对象获取用户信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String username = authentication.getName();
            // 这里可以通过用户名查询用户ID，或者从JWT中直接获取
            // 暂时从JWT token中获取用户ID
        }
        
        // 从Authorization header获取JWT token
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        
        throw new RuntimeException("无法获取用户ID，请检查认证状态");
    }

    public static class AddToCartRequest {
        private Long productId;
        private Integer quantity;
        
        public AddToCartRequest() {}
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
    
    public static class UpdateCartRequest {
        private Long productId;
        private Integer quantity;
        
        public UpdateCartRequest() {}
        
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }
}