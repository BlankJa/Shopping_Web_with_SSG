package org.example.startup.config;

import org.example.startup.filter.JwtAuthenticationFilter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Value("${cors.allowed-origins}")
    private String allowedOrigins;
    
    @Value("${cors.allowed-methods}")
    private String allowedMethods;
    
    @Value("${cors.allowed-headers}")
    private String allowedHeaders;
    
    @Value("${cors.allow-credentials}")
    private boolean allowCredentials;
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(request -> {
                var corsConfiguration = new org.springframework.web.cors.CorsConfiguration();
                corsConfiguration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
                corsConfiguration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
                corsConfiguration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
                corsConfiguration.setAllowCredentials(allowCredentials);
                return corsConfiguration;
            })) // 启用CORS支持，配置从配置文件读取
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 公开接口
                .requestMatchers("/api/user/login", "/api/user/register", "/api/products", "/api/products/**").permitAll()
                // 静态资源
                .requestMatchers("/static/**", "/images/**", "/css/**", "/js/**", "/*.html", "/").permitAll()
                // 购物车接口 - 需要认证
                .requestMatchers("/api/cart/**").authenticated()
                // 测试接口 - 需要认证
                .requestMatchers("/api/test/**").authenticated()
                // 管理员接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                // 用户管理接口
                .requestMatchers("/api/user/**").hasAnyRole("USER", "ADMIN", "MERCHANT")
                // 其他接口需要认证
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}