package org.example.startup.dto;

import org.example.startup.model.CartItem;
import org.example.startup.model.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CartItemDto {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private String productDescription;
    private BigDecimal productPrice;
    private String productImageUrl;
    private Integer productStock;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public CartItemDto() {}
    
    public CartItemDto(CartItem cartItem) {
        this.id = cartItem.getId();
        this.userId = cartItem.getUserId();
        this.productId = cartItem.getProductId();
        this.quantity = cartItem.getQuantity();
        this.createdAt = cartItem.getCreatedAt();
        this.updatedAt = cartItem.getUpdatedAt();
        
        // 如果有关联的商品信息
        if (cartItem.getProduct() != null) {
            Product product = cartItem.getProduct();
            this.productName = product.getName();
            this.productDescription = product.getDescription();
            this.productPrice = product.getPrice();
            this.productImageUrl = product.getImageUrl();
            this.productStock = product.getStock();
            
            // 计算总价
            if (product.getPrice() != null && this.quantity != null) {
                this.totalPrice = product.getPrice().multiply(BigDecimal.valueOf(this.quantity));
            }
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public Long getProductId() {
        return productId;
    }
    
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    
    public String getProductName() {
        return productName;
    }
    
    public void setProductName(String productName) {
        this.productName = productName;
    }
    
    public String getProductDescription() {
        return productDescription;
    }
    
    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }
    
    public BigDecimal getProductPrice() {
        return productPrice;
    }
    
    public void setProductPrice(BigDecimal productPrice) {
        this.productPrice = productPrice;
    }
    
    public String getProductImageUrl() {
        return productImageUrl;
    }
    
    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }
    
    public Integer getProductStock() {
        return productStock;
    }
    
    public void setProductStock(Integer productStock) {
        this.productStock = productStock;
    }
    
    public Integer getQuantity() {
        return quantity;
    }
    
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
    
    public BigDecimal getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(BigDecimal totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}