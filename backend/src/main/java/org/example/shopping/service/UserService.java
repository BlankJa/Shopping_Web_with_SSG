package org.example.startup.service;

import org.example.startup.model.User;
import org.example.startup.model.Role;
import org.example.startup.repository.UserRepository;
import org.example.startup.repository.RoleRepository;
import org.example.startup.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    public User login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null) {
                return null;
            }
            if (!user.isEnabled()) {
                return null;
            }
            
            if (passwordEncoder.matches(password, user.getPassword())) {
                // 更新最后登录时间
                user.setLastLogin(LocalDateTime.now());
                userRepository.save(user);
                
                // 调试：检查用户角色信息
                return user;
            } else {
                return null;
            }
        } catch (Exception e) {

            throw e;
        }
    }
    
    public boolean register(User user) {
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()) != null) {
                return false;
            }
            // 密码加密
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            // 为新用户分配默认角色
            Optional<Role> defaultRole = roleRepository.findByName("USER");
            if (defaultRole.isPresent()) {
                user.addRole(defaultRole.get());
            } 
            // 保存用户
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }
    /**
     * 生成JWT令牌
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user);
    }
    
    /**
     * 为用户添加角色
     */
    public void addRoleToUser(String username, String roleName) {

        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("角色不存在: " + roleName);
        }
        
        user.addRole(roleOpt.get());
        userRepository.save(user);
        

    }
    
    /**
     * 从用户中移除角色
     */
    public void removeRoleFromUser(String username, String roleName) {

        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {

            return;
        }
        
        user.removeRole(roleOpt.get());
        userRepository.save(user);
        

    }
    
    /**
     * 获取用户的所有角色
     */
    public Set<Role> getUserRoles(String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        return user.getRoles();
    }
    
    /**
     * 启用/禁用用户
     */
    public void setUserEnabled(String username, boolean enabled) {

        
        User user = userRepository.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在: " + username);
        }
        
        user.setEnabled(enabled);
        userRepository.save(user);
        

    }
    
    /**
     * 根据用户名查找用户
     */
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    /**
     * 检查用户是否有特定权限
     */
    public boolean hasPermission(String username, String permissionName) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        return user.hasPermission(permissionName);
    }
    
    /**
     * 检查用户是否有特定角色
     */
    public boolean hasRole(String username, String roleName) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            return false;
        }
        return user.hasRole(roleName);
    }
    
    /**
     * 根据ID查找用户
     */
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
    
    /**
     * 保存用户
     */
    public User save(User user) {
        return userRepository.save(user);
    }
}