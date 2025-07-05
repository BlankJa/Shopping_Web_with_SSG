package org.example.startup.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.startup.util.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getJwtFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    // 验证令牌
                    if (jwtUtil.validateToken(token, username)) {
                        // 从令牌中获取角色和权限信息
                        String roles = jwtUtil.getRolesFromToken(token);
                        String permissions = jwtUtil.getPermissionsFromToken(token);

                        // 创建权限列表
                        List<SimpleGrantedAuthority> authorities = createAuthorities(roles, permissions);
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // 设置到安全上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        // 用户通过JWT认证成功
                    }
                }
            }
        } catch (Exception e) {
            logger.error("JWT authentication failed: ", e);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Authentication failed: " + e.getMessage().replace("\"", "\\\"") + "\"}");
            return;
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 从请求中提取JWT令牌
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
    
    /**
     * 创建权限列表
     */
    private List<SimpleGrantedAuthority> createAuthorities(String roles, String permissions) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles.split(","))
                .filter(StringUtils::hasText)
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.trim()))
                .collect(Collectors.toList());
        
        // 添加权限
        if (StringUtils.hasText(permissions)) {
            List<SimpleGrantedAuthority> permissionAuthorities = Arrays.stream(permissions.split(","))
                    .filter(StringUtils::hasText)
                    .map(permission -> new SimpleGrantedAuthority(permission.trim()))
                    .collect(Collectors.toList());
            authorities.addAll(permissionAuthorities);
        }
        
        return authorities;
    }
}