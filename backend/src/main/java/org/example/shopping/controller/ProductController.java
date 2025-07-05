package org.example.startup.controller;

import org.example.startup.model.Product;
import org.example.startup.service.ProductService;
import org.example.startup.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/products")
public class ProductController {
    
    @Autowired
    private ProductService productService;
    
    @GetMapping
    public ResponseEntity<?> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "popularity") String sort) {
        try {
            // 如果没有分页参数，返回所有产品（保持向后兼容）
            if (page == 0 && size == 8 && "popularity".equals(sort)) {
                // 检查是否有其他查询参数，如果没有则可能是简单的获取所有产品请求
                PageResponse<Product> pageResponse = productService.getProductsWithPagination(page, size, sort);
                return new ResponseEntity<>(pageResponse, HttpStatus.OK);
            } else {
                // 明确的分页请求
                PageResponse<Product> pageResponse = productService.getProductsWithPagination(page, size, sort);
                return new ResponseEntity<>(pageResponse, HttpStatus.OK);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Product>> getAllProductsList() {
        try {
            List<Product> products = productService.getAllProducts();
            return new ResponseEntity<>(products, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Product> getProductById(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return new ResponseEntity<>(product, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        try {
            Product savedProduct = productService.saveProduct(product);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                product.setId(id);
                Product updatedProduct = productService.saveProduct(product);
                return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
            } else {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {
        try {
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                productService.deleteProduct(id);
                return new ResponseEntity<>("Product deleted successfully", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("Product not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("Error query product", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 更新产品库存
     * @param id 产品ID
     * @param stock 新的库存数量
     * @return 更新后的产品信息
     */
    @PutMapping("/{id}/stock")
     public ResponseEntity<?> updateProductStock(@PathVariable Long id, @RequestParam Integer stock) {
        try {
            if (stock < 0) {
                return new ResponseEntity<>("库存数量不能为负数", HttpStatus.BAD_REQUEST);
            }
            
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                existingProduct.setStock(stock);
                Product updatedProduct = productService.saveProduct(existingProduct);
                return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("产品不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("更新库存失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 减少产品库存（用于下单等场景）
     * @param id 产品ID
     * @param quantity 减少的数量
     * @return 更新后的产品信息
     */
    @PutMapping("/{id}/stock/reduce")
     public ResponseEntity<?> reduceProductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            if (quantity <= 0) {
                return new ResponseEntity<>("减少数量必须大于0", HttpStatus.BAD_REQUEST);
            }
            
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                Integer currentStock = existingProduct.getStock();
                if (currentStock < quantity) {
                    return new ResponseEntity<>("库存不足，当前库存: " + currentStock, HttpStatus.BAD_REQUEST);
                }
                
                existingProduct.setStock(currentStock - quantity);
                Product updatedProduct = productService.saveProduct(existingProduct);
                return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("产品不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("减少库存失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 增加产品库存（用于进货等场景）
     * @param id 产品ID
     * @param quantity 增加的数量
     * @return 更新后的产品信息
     */
    @PutMapping("/{id}/stock/add")
     public ResponseEntity<?> addProductStock(@PathVariable Long id, @RequestParam Integer quantity) {
        try {
            if (quantity <= 0) {
                return new ResponseEntity<>("增加数量必须大于0", HttpStatus.BAD_REQUEST);
            }
            
            Product existingProduct = productService.getProductById(id);
            if (existingProduct != null) {
                Integer currentStock = existingProduct.getStock();
                existingProduct.setStock(currentStock + quantity);
                Product updatedProduct = productService.saveProduct(existingProduct);
                return new ResponseEntity<>(updatedProduct, HttpStatus.OK);
            } else {
                return new ResponseEntity<>("产品不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("增加库存失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 检查产品库存
     * @param id 产品ID
     * @return 库存信息
     */
    @GetMapping("/{id}/stock")
     public ResponseEntity<?> getProductStock(@PathVariable Long id) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                return new ResponseEntity<>(new StockInfo(product.getId(), product.getName(), product.getStock()), HttpStatus.OK);
            } else {
                return new ResponseEntity<>("产品不存在", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("查询库存失败", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 库存信息DTO
     */
    public static class StockInfo {
        private Long productId;
        private String productName;
        private Integer stock;
        
        public StockInfo(Long productId, String productName, Integer stock) {
            this.productId = productId;
            this.productName = productName;
            this.stock = stock;
        }
        
        // Getters
        public Long getProductId() { return productId; }
        public String getProductName() { return productName; }
        public Integer getStock() { return stock; }
        
        // Setters
        public void setProductId(Long productId) { this.productId = productId; }
        public void setProductName(String productName) { this.productName = productName; }
        public void setStock(Integer stock) { this.stock = stock; }
    }
}