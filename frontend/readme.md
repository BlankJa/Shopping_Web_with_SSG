# Shopping Web Frontend

基于React和Vite构建的现代电商网站前端项目，提供流畅的用户体验和丰富的购物功能。

## 项目特点

- 基于Vite的快速开发环境
- React 18的新特性支持
- 响应式设计
- SSG静态站点生成支持
- 优化的性能和加载速度

## 技术栈

- React 18
- Vite
- React Router DOM
- Axios
- Bootstrap
- React Bootstrap

## 项目结构

```
src/
├── components/        # 可重用组件
│   ├── ErrorMessage/  # 错误提示组件
│   ├── Loading/       # 加载状态组件
│   ├── Navbar/        # 导航栏组件
│   └── ProductCard/   # 商品卡片组件
├── pages/            # 页面组件
│   ├── Home/         # 首页
│   ├── Products/     # 商品列表页
│   └── ProductDetail/ # 商品详情页
├── utils/            # 工具函数
│   ├── formatters.js # 格式化工具
│   └── useFetch.js   # 数据获取Hook
├── contexts/         # React上下文
├── App.jsx          # 根组件
└── index.jsx        # 入口文件
```

## 主要功能

### 1. 商品展示
- 商品列表展示
- 商品详情页
- 商品搜索和筛选
- 商品分类浏览

### 2. 购物车功能
- 添加/删除商品
- 修改商品数量
- 购物车总价计算
- 结算功能

### 3. 用户功能
- 用户注册/登录
- 个人信息管理
- 订单历史查看
- 收货地址管理

### 4. 性能优化
- 图片懒加载
- 路由懒加载
- 状态管理优化
- SSG静态生成

## 开发环境设置

### 安装依赖
```bash
npm install
```

### 开发服务器启动
```bash
npm run dev
```

### 生产环境构建
```bash
npm run build
```

### 预览构建结果
```bash
npm run preview
```

## 环境变量配置

在`.env`文件中配置开发环境变量：
```env
VITE_API_URL=http://localhost:8080/api
VITE_PUBLIC_URL=/
```

在`.env.production`文件中配置生产环境变量：
```env
VITE_API_URL=https://api.example.com/api
VITE_PUBLIC_URL=/shopping/
```

## 开发指南

### 组件开发规范
- 使用函数组件和Hooks
- 遵循React最佳实践
- 组件文档和类型定义
- 统一的错误处理

### 状态管理
- 使用React Context
- 合理的状态分层
- 缓存策略

### 样式管理
- 基于Bootstrap的响应式设计
- 自定义主题
- 组件级CSS


## 性能优化建议

1. 代码分割
   - 路由级别分割
   - 组件懒加载
   - 第三方库按需导入

2. 资源优化
   - 图片优化
   - 静态资源CDN
   - 缓存策略

3. 渲染优化
   - 虚拟列表
   - 防抖和节流
   - 状态更新批处理