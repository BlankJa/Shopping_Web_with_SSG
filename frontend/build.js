#!/usr/bin/env node

const { execSync } = require('child_process');
const fs = require('fs');
const path = require('path');

console.log('🚀 开始构建静态化项目...');

try {
  // 清理之前的构建文件
  console.log('📦 清理构建目录...');
  if (fs.existsSync('dist')) {
    execSync('rmdir /s /q dist', { stdio: 'inherit' });
  }

  // 安装依赖
  console.log('📥 检查依赖...');
  execSync('npm install', { stdio: 'inherit' });

  // 构建项目
  console.log('🔨 构建项目...');
  execSync('npm run build', { stdio: 'inherit' });

  // 创建部署说明文件
  const deploymentGuide = `# 静态化部署说明

## 构建完成

项目已成功构建为静态文件，位于 \`dist\` 目录中。

## 部署步骤

1. 将 \`dist\` 目录中的所有文件上传到您的静态文件服务器
2. 确保您的后端API服务正在运行
3. 根据实际的后端API地址修改 \`.env.production\` 文件中的 \`VITE_API_BASE_URL\`
4. 如果需要重新构建，请运行 \`npm run build\`

## 注意事项

- 确保后端API支持CORS跨域请求
- 如果使用CDN，请确保API请求不被缓存
- 建议配置适当的缓存策略

## 后端API要求

项目保留了所有后端API接口调用，请确保以下接口可用：

- \`GET /api/products\` - 获取商品列表
- \`GET /api/products/categories\` - 获取商品分类
- \`GET /api/products/{id}\` - 获取商品详情
- 其他相关的用户认证和购物车API

构建时间: ${new Date().toLocaleString()}
`;

  fs.writeFileSync(path.join('dist', 'DEPLOYMENT.md'), deploymentGuide);

  console.log('✅ 构建完成！');
  console.log('📁 静态文件已生成到 dist 目录');
  console.log('📖 请查看 dist/DEPLOYMENT.md 了解部署说明');

} catch (error) {
  console.error('❌ 构建失败:', error.message);
  process.exit(1);
}