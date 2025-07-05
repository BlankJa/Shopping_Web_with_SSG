package org.example.startup.service;

import org.example.startup.model.Role;
import org.example.startup.model.Permission;
import org.example.startup.repository.RoleRepository;
import org.example.startup.repository.PermissionRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@Transactional
public class RoleService {
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PermissionRepository permissionRepository;
    /**
     * 创建角色
     */
    public Role createRole(String name, String description) {
        if (roleRepository.existsByName(name)) {
            throw new RuntimeException("角色已存在: " + name);
        }
        Role role = new Role(name, description);
        Role savedRole = roleRepository.save(role);
        return savedRole;
    }
    
    /**
     * 根据名称查找角色
     */
    public Optional<Role> findByName(String name) {
        return roleRepository.findByName(name);
    }
    
    /**
     * 获取所有角色
     */
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
    
    /**
     * 为角色添加权限
     */
    public void addPermissionToRole(String roleName, String permissionName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName);
        
        if (roleOpt.isEmpty()) {

            throw new RuntimeException("角色不存在: " + roleName);
        }
        
        if (permissionOpt.isEmpty()) {

            throw new RuntimeException("权限不存在: " + permissionName);
        }
        
        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();
        
        role.addPermission(permission);
        roleRepository.save(role);
        

    }
    
    /**
     * 从角色中移除权限
     */
    public void removePermissionFromRole(String roleName, String permissionName) {

        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        Optional<Permission> permissionOpt = permissionRepository.findByName(permissionName);
        
        if (roleOpt.isEmpty() || permissionOpt.isEmpty()) {

            return;
        }
        
        Role role = roleOpt.get();
        Permission permission = permissionOpt.get();
        
        role.removePermission(permission);
        roleRepository.save(role);
        

    }
    
    /**
     * 获取角色的所有权限
     */
    public Set<Permission> getRolePermissions(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("角色不存在: " + roleName);
        }
        return roleOpt.get().getPermissions();
    }
    
    /**
     * 删除角色
     */
    public void deleteRole(String roleName) {

        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {

            return;
        }
        
        Role role = roleOpt.get();
        
        // 检查是否有用户使用此角色
        if (!role.getUsers().isEmpty()) {

            throw new RuntimeException("无法删除角色，仍有用户使用此角色: " + roleName);
        }
        
        roleRepository.delete(role);

    }
    
    /**
     * 更新角色信息
     */
    public Role updateRole(String roleName, String newDescription) {

        
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (roleOpt.isEmpty()) {
            throw new RuntimeException("角色不存在: " + roleName);
        }
        
        Role role = roleOpt.get();
        role.setDescription(newDescription);
        
        Role updatedRole = roleRepository.save(role);

        
        return updatedRole;
    }
}