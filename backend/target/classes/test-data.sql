
-- 产品测试数据
-- 插入示例产品数据

INSERT INTO products (name, description, price, image_url, popularity, stock) VALUES
('智能手机', '最新款智能手机，配备高清摄像头和快速处理器', 2999.00, 'http://localhost:8080/images/example.png', 95, 50),
('笔记本电脑', '轻薄便携笔记本电脑，适合办公和学习', 5999.00, 'http://localhost:8080/images/example.png', 88, 30),
('无线耳机', '高品质无线蓝牙耳机，降噪效果出色', 299.00, 'http://localhost:8080/images/example.png', 92, 100),
('智能手表', '多功能智能手表，支持健康监测和运动追踪', 1299.00, 'http://localhost:8080/images/example.png', 85, 75),
('平板电脑', '10英寸高清屏幕平板电脑，娱乐办公两不误', 2199.00, 'http://localhost:8080/images/example.png', 78, 40),
('游戏手柄', '专业游戏手柄，支持多平台兼容', 199.00, 'http://localhost:8080/images/example.png', 82, 80),
('充电宝', '大容量移动电源，快充技术支持', 89.00, 'http://localhost:8080/images/example.png', 90, 200),
('蓝牙音箱', '便携式蓝牙音箱，音质清晰响亮', 159.00, 'http://localhost:8080/images/example.png', 87, 60),
('数据线', 'USB-C快充数据线，传输速度快', 29.00, 'http://localhost:8080/images/example.png', 75, 150),
('手机壳', '防摔透明手机保护壳，轻薄设计', 39.00, 'http://localhost:8080/images/example.png', 70, 120),
('键盘', '机械键盘，青轴手感，RGB背光', 399.00, 'http://localhost:8080/images/example.png', 83, 45),
('鼠标', '无线游戏鼠标，高精度传感器', 199.00, 'http://localhost:8080/images/example.png', 81, 65),
('显示器', '27英寸4K显示器，色彩还原度高', 1899.00, 'http://localhost:8080/images/example.png', 86, 25),
('摄像头', '高清网络摄像头，支持1080P录制', 299.00, 'http://localhost:8080/images/example.png', 79, 35),
('路由器', '千兆无线路由器，信号覆盖广', 199.00, 'http://localhost:8080/images/example.png', 84, 55);

-- 更新产品流行度（可选）
UPDATE products SET popularity = FLOOR(RAND() * 100) + 1 WHERE id > 0;

-- 查询验证数据
SELECT COUNT(*) as total_products FROM products;
SELECT * FROM products ORDER BY popularity DESC LIMIT 5;