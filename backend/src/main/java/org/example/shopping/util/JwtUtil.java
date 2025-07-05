package org.example.startup.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.startup.model.User;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class JwtUtil {
    // JWT密钥，在生产环境中应该从配置文件或环境变量中读取
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;
    
    // JWT过期时间（毫秒）- 默认24小时
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * 生成JWT令牌
     */
    public String generateToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("username", user.getUsername());
        claims.put("email", user.getEmail());
        claims.put("enabled", user.isEnabled());
        
        // 添加角色信息
        String roles = user.getRoles().stream()
                .map(role -> role.getName())
                .collect(Collectors.joining(","));
        claims.put("roles", roles);
        
        // 添加权限信息
        String permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(permission -> permission.getName())
                .distinct()
                .collect(Collectors.joining(","));
        claims.put("permissions", permissions);
        
        return createToken(claims, user.getUsername());
    }
    
    /**
     * 创建令牌
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                .compact();
    }
    
    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }
    
    /**
     * 从令牌中获取角色
     */
    public String getRolesFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("roles", String.class);
    }
    
    /**
     * 从令牌中获取权限
     */
    public String getPermissionsFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("permissions", String.class);
    }
    
    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    /**
     * 从令牌中获取特定声明
     */
    public <T> T getClaimFromToken(String token, ClaimsResolver<T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.resolve(claims);
    }
    
    /**
     * 从令牌中获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {

            throw e;
        } catch (UnsupportedJwtException e) {

            throw e;
        } catch (MalformedJwtException e) {

            throw e;
        } catch (SecurityException e) {

            throw e;
        } catch (IllegalArgumentException e) {

            throw e;
        }
    }
    
    /**
     * 检查令牌是否过期
     */
    public boolean isTokenExpired(String token) {
        try {
            final Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }
    
    /**
     * 验证令牌
     */
    public boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = getUsernameFromToken(token);
            return (username.equals(tokenUsername) && !isTokenExpired(token));
        } catch (Exception e) {

            return false;
        }
    }
    
    /**
     * 验证令牌（不需要用户名）
     */
    public boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {

            return false;
        }
    }
    
    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        try {
            final Claims claims = getAllClaimsFromToken(token);
            claims.setIssuedAt(new Date());
            claims.setExpiration(new Date(System.currentTimeMillis() + jwtExpiration));
            
            return Jwts.builder()
                    .setClaims(claims)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS512)
                    .compact();
        } catch (Exception e) {

            throw new RuntimeException("无法刷新令牌", e);
        }
    }
    
    @FunctionalInterface
    public interface ClaimsResolver<T> {
        T resolve(Claims claims);
    }
}