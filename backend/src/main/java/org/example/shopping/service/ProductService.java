package org.example.startup.service;

import org.example.startup.model.Product;
import org.example.startup.repository.ProductRepository;
import org.example.startup.dto.PageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private RedissonStockService redissonStockService;
    
    public List<Product> getAllProducts() {
        // 如果数据库中没有数据，返回示例数据
        List<Product> products = productRepository.findAll();
        return products;
    }
    
    public PageResponse<Product> getProductsWithPagination(int page, int size, String sort) {
        // 创建排序对象
        Sort sortObj;
        if ("price".equals(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "price");
        } else {
            sortObj = Sort.by(Sort.Direction.DESC, "popularity");
        }
        
        // 创建分页对象
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        // 获取分页数据
        Page<Product> productPage = productRepository.findAll(pageable);
        
        // 返回数据库分页结果
        return new PageResponse<>(
            productPage.getContent(),
            productPage.getTotalPages(),
            productPage.getTotalElements(),
            productPage.getSize(),
            productPage.getNumber()
        );
    }
    
    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
    
    public Product saveProduct(Product product) {
        Product savedProduct = productRepository.save(product);
        // 同步库存到Redis
        if (savedProduct.getId() != null && savedProduct.getStock() != null) {
            redissonStockService.initStock(savedProduct.getId(), savedProduct.getStock());
        }
        return savedProduct;
    }
    
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        // 清理Redis中的库存缓存
        redissonStockService.deleteStock(id);
    }
}