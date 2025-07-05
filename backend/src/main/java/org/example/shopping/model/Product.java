package org.example.startup.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "image_url")
    @JsonProperty("imageUrl")
    private String imageUrl;
    
    @Column
    private Integer popularity;
    
    @Column(nullable = false)
    private Integer stock = 0;
    
    public Product() {}
    
    public Product(String name, String description, BigDecimal price, String imageUrl, Integer popularity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.popularity = popularity;
        this.stock = 0;
    }
    
    public Product(String name, String description, BigDecimal price, String imageUrl, Integer popularity, Integer stock) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.popularity = popularity;
        this.stock = stock;
    }
    
    // 为了兼容前端，添加一个返回double类型价格的方法
    @JsonProperty("price")
    public double getPriceAsDouble() {
        return price != null ? price.doubleValue() : 0.0;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public Integer getPopularity() {
        return popularity;
    }
    
    public void setPopularity(Integer popularity) {
        this.popularity = popularity;
    }
    
    public Integer getStock() {
        return stock;
    }
    
    public void setStock(Integer stock) {
        this.stock = stock;
    }
}