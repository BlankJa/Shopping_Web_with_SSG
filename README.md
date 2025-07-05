# Shopping Web with SSG

一个基于Spring Boot和React的现代电商网站，支持静态站点生成（SSG）功能。

## 项目概述

本项目是一个全栈电商应用，采用前后端分离架构，具有完整的用户认证、商品管理、购物车和订单处理等功能。项目特别之处在于支持静态站点生成，可以提供更好的性能和SEO优化。

## 技术栈

### 后端技术
- Spring Boot 3.4.5
- Spring Security（认证和授权）
- Spring Data JPA（数据持久化）
- MySQL（数据库）
- Redis（缓存和分布式锁）
- Redisson（分布式服务）
- JWT（用户令牌）

### 前端技术
- React 18
- React Router v6
- React Bootstrap
- Vite（构建工具）
- Axios（HTTP客户端）

## 主要功能

- 用户认证和授权
- 商品浏览和搜索
- 购物车管理
- 订单处理
- 静态页面生成
- 分布式锁防止超卖
- Redis缓存优化

## 开发环境要求

- JDK 17
- Node.js 16+
- MySQL 8.0+
- Redis 6.0+

## 快速开始

### 后端服务启动

1. 进入backend目录：
```bash
cd backend
```

2. 使用Maven构建项目：
```bash
mvn clean install
```

3. 运行Spring Boot应用：
```bash
mvn spring-boot:run
```

### 前端应用启动

1. 进入frontend目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

4. 构建静态站点：
```bash
serve -s dist -l 3000
```

## 项目结构

```
├── backend/                # 后端Spring Boot项目
│   ├── src/               # 源代码
│   └── pom.xml           # Maven配置
├── frontend/              # 前端React项目
│   ├── src/              # 源代码
│   ├── public/           # 静态资源
│   └── package.json      # npm配置
```
