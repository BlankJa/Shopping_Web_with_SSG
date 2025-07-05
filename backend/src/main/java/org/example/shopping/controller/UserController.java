package org.example.startup.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.example.startup.service.UserService;
import org.example.startup.model.User;
import org.example.startup.model.Role;
import org.example.startup.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import jakarta.servlet.http.HttpServletRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/index")
    public String index(HttpServletRequest request) {
        return "hello";
    }
    
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody User user, HttpServletRequest request) {
        try {
            User loginUser = userService.login(user.getUsername(), user.getPassword());
            if (loginUser != null) {
                // 生成JWT令牌
                String token = userService.generateToken(loginUser);
                // 构建响应数据
                Map<String, Object> response = new HashMap<>();
                response.put("message", "登录成功");
                response.put("token", token);
                response.put("user", Map.of(
                    "id", loginUser.getId(),
                    "username", loginUser.getUsername(),
                    "email", loginUser.getEmail() != null ? loginUser.getEmail() : "",
                    "roles", loginUser.getRoles().stream().map(Role::getName).toList()
                ));
                

                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "用户名或密码错误");
                

                return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("message", "登录异常");
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody User user, HttpServletRequest request) {
        try {
            boolean success = userService.register(user);
            if (success) {
                return new ResponseEntity<>("注册成功", HttpStatus.OK);
            } else {
                return new ResponseEntity<>("用户名已存在，注册失败", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception e) {
            return new ResponseEntity<>("注册异常", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    // =================== 用户管理接口 ====================
    
    @GetMapping("/profile")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(Map.of("message", "用户不存在"), HttpStatus.NOT_FOUND);
        }
        
        Map<String, Object> profile = new HashMap<>();
        profile.put("id", user.getId());
        profile.put("username", user.getUsername());
        profile.put("email", user.getEmail());
        profile.put("enabled", user.isEnabled());
        profile.put("createdAt", user.getCreatedAt());
        profile.put("lastLogin", user.getLastLogin());
        profile.put("roles", user.getRoles().stream().map(Role::getName).toList());
        profile.put("balance", user.getBalance());
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }
    
    /**
     * 获取用户余额
     */
    @GetMapping("/balance")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> getBalance(HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
            }
            return ResponseEntity.ok(Map.of("balance", user.getBalance()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 充值余额
     */
    @PostMapping("/recharge")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<?> recharge(@RequestBody RechargeRequest request, HttpServletRequest httpRequest) {
        try {
            Long userId = getUserIdFromToken(httpRequest);
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "用户不存在"));
            }
            
            if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "充值金额必须大于0"));
            }
            
            // 增加余额
            BigDecimal newBalance = user.getBalance().add(request.getAmount());
            user.setBalance(newBalance);
            userService.save(user);
            
            return ResponseEntity.ok(Map.of(
                "message", "充值成功",
                "amount", request.getAmount(),
                "balance", newBalance
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    /**
     * 从JWT token中获取用户ID
     */
    private Long getUserIdFromToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new RuntimeException("无法获取用户ID，请检查认证状态");
    }
    
    /**
     * 充值请求DTO
     */
    public static class RechargeRequest {
        private BigDecimal amount;
        
        public RechargeRequest() {}
        
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
    }
}
