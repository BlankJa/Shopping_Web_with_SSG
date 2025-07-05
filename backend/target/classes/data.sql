-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP,
    last_login TIMESTAMP,
    balance DECIMAL(10,2) DEFAULT 0.00,
    INDEX idx_username (username)
);

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    INDEX idx_name (name)
);

-- 创建权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(255),
    resource VARCHAR(255),
    action VARCHAR(255),
    INDEX idx_name (name),
    INDEX idx_resource_action (resource, action)
);

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    role_id BIGINT NOT NULL,
    permission_id BIGINT NOT NULL,
    PRIMARY KEY (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE
);

-- 插入默认角色
INSERT INTO roles (name, description) VALUES 
('ADMIN', '管理员角色，拥有所有权限'),
('USER', '普通用户角色，拥有基本权限'),
('MERCHANT', '商家角色，拥有商家管理权限')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 插入默认权限
INSERT INTO permissions (name, description, resource, action) VALUES 
('USER_READ', '查看用户信息', 'user', 'read'),
('USER_WRITE', '修改用户信息', 'user', 'write'),
('USER_DELETE', '删除用户', 'user', 'delete'),
('ROLE_READ', '查看角色信息', 'role', 'read'),
('ROLE_WRITE', '修改角色信息', 'role', 'write'),
('ROLE_DELETE', '删除角色', 'role', 'delete'),
('PERMISSION_READ', '查看权限信息', 'permission', 'read'),
('PERMISSION_WRITE', '修改权限信息', 'permission', 'write'),
('PERMISSION_DELETE', '删除权限', 'permission', 'delete'),
('SYSTEM_ADMIN', '系统管理', 'system', 'admin'),
('PRODUCT_READ', '查看商品信息', 'product', 'read'),
('PRODUCT_WRITE', '管理商品信息', 'product', 'write'),
('PRODUCT_DELETE', '删除商品', 'product', 'delete'),
('ORDER_READ', '查看订单信息', 'order', 'read'),
('ORDER_WRITE', '处理订单', 'order', 'write'),
('STORE_MANAGE', '店铺管理', 'store', 'manage')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 为ADMIN角色分配所有权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为USER角色分配基本权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'USER' AND p.name IN ('USER_READ')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为MERCHANT角色分配商家权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'MERCHANT' AND p.name IN ('USER_READ', 'PRODUCT_READ', 'PRODUCT_WRITE', 'PRODUCT_DELETE', 'ORDER_READ', 'ORDER_WRITE', 'STORE_MANAGE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 创建默认管理员用户 (密码: admin123)
INSERT INTO users (username, password, email, enabled, created_at) VALUES 
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8ioctKQVkwrpNhaAVv2PzqC6.9WTO', 'admin@example.com', true, NOW())
ON DUPLICATE KEY UPDATE email = VALUES(email);

-- 为默认管理员分配ADMIN角色
INSERT INTO user_roles (user_id, role_id) 
SELECT u.id, r.id 
FROM users u, roles r 
WHERE u.username = 'admin' AND r.name = 'ADMIN'
ON DUPLICATE KEY UPDATE user_id = VALUES(user_id);

-- 创建产品表
CREATE TABLE IF NOT EXISTS products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    image_url VARCHAR(500),
    popularity INT DEFAULT 0,
    stock INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_price (price),
    INDEX idx_popularity (popularity),
    INDEX idx_stock (stock)
);

-- 创建购物车表
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_product (user_id, product_id),
    INDEX idx_user_id (user_id),
    INDEX idx_product_id (product_id),
    INDEX idx_created_at (created_at)
);

-- 添加购物车相关权限
INSERT INTO permissions (name, description, resource, action) VALUES 
('CART_READ', '查看购物车', 'cart', 'read'),
('CART_WRITE', '管理购物车', 'cart', 'write')
ON DUPLICATE KEY UPDATE description = VALUES(description);

-- 为USER角色添加购物车权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'USER' AND p.name IN ('CART_READ', 'CART_WRITE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);

-- 为MERCHANT角色添加购物车权限
INSERT INTO role_permissions (role_id, permission_id) 
SELECT r.id, p.id 
FROM roles r, permissions p 
WHERE r.name = 'MERCHANT' AND p.name IN ('CART_READ', 'CART_WRITE')
ON DUPLICATE KEY UPDATE role_id = VALUES(role_id);