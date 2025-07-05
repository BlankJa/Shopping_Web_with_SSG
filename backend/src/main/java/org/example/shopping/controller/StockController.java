package org.example.startup.controller;

import org.example.startup.service.RedissonStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/stock")
public class StockController {

    @Autowired
    private RedissonStockService redissonStockService;

    /**
     * 获取商品库存
     */
    @GetMapping("/{productId}")
    public ResponseEntity<?> getStock(@PathVariable Long productId) {
        try {
            Integer stock = redissonStockService.getStock(productId);
            return ResponseEntity.ok(Map.of(
                "productId", productId,
                "stock", stock != null ? stock : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 从数据库同步库存到Redis
     */
    @PostMapping("/sync/{productId}")
    public ResponseEntity<?> syncStock(@PathVariable Long productId) {
        try {
            redissonStockService.syncStockFromDB(productId);
            Integer stock = redissonStockService.getStock(productId);
            return ResponseEntity.ok(Map.of(
                "message", "库存同步成功",
                "productId", productId,
                "stock", stock != null ? stock : 0
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 同步Redis库存到数据库
     */
    @PostMapping("/sync-to-db/{productId}")
    public ResponseEntity<?> syncStockToDB(@PathVariable Long productId) {
        try {
            redissonStockService.syncStockToDB(productId);
            return ResponseEntity.ok(Map.of(
                "message", "库存已同步到数据库",
                "productId", productId
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * 手动设置商品库存（仅用于测试）
     */
    @PostMapping("/set/{productId}")
    public ResponseEntity<?> setStock(@PathVariable Long productId, @RequestBody Map<String, Integer> request) {
        try {
            Integer stock = request.get("stock");
            if (stock == null || stock < 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "库存数量必须大于等于0"));
            }
            
            redissonStockService.initStock(productId, stock);
            return ResponseEntity.ok(Map.of(
                "message", "库存设置成功",
                "productId", productId,
                "stock", stock
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}