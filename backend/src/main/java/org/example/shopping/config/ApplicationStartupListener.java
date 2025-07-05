package org.example.startup.config;

import org.example.startup.model.Product;
import org.example.startup.repository.ProductRepository;
import org.example.startup.service.RedissonStockService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApplicationStartupListener implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationStartupListener.class);

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private RedissonStockService redissonStockService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("应用启动，开始同步商品库存到Redis...");
        
        try {
            List<Product> products = productRepository.findAll();
            int syncCount = 0;
            
            for (Product product : products) {
                if (product.getId() != null && product.getStock() != null) {
                    redissonStockService.initStock(product.getId(), product.getStock());
                    syncCount++;
                }
            }
            
            logger.info("库存同步完成，共同步 {} 个商品的库存信息到Redis", syncCount);
        } catch (Exception e) {
            logger.error("库存同步失败: {}", e.getMessage(), e);
        }
    }
}