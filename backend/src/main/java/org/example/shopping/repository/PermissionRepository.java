package org.example.startup.repository;

import org.example.startup.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByName(String name);
    List<Permission> findByResource(String resource);
    List<Permission> findByAction(String action);
    Optional<Permission> findByResourceAndAction(String resource, String action);
    boolean existsByName(String name);
}