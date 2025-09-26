# AI智能体前端项目

这是一个基于Vue3的AI智能体前端应用，包含两个主要的AI聊天应用。

## 功能特性

- 🏠 **主页**: 用于切换不同的AI应用
- 💕 **AI恋爱大师**: 专业的恋爱咨询AI助手
- 🤖 **AI超级智能体**: 强大的多功能AI助手
- 🔄 **实时聊天**: 基于SSE的实时对话功能
- 📱 **响应式设计**: 适配各种设备尺寸

## 技术栈

- Vue 3
- Vue Router 4
- Pinia (状态管理)
- Axios (HTTP请求)
- Vite (构建工具)

## 项目结构

```
src/
├── api/           # API配置
├── router/        # 路由配置
├── views/         # 页面组件
│   ├── Home.vue          # 主页
│   ├── LoveApp.vue       # AI恋爱大师
│   └── ManusApp.vue      # AI超级智能体
├── App.vue        # 根组件
├── main.js        # 入口文件
└── style.css      # 全局样式
```

## 安装和运行

### 1. 安装依赖

```bash
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

项目将在 http://localhost:3000 启动

### 3. 构建生产版本

```bash
npm run build
```

## 后端接口

项目需要配合SpringBoot后端运行，后端接口地址为：`http://localhost:8123/api`

### 接口说明

1. **AI恋爱大师接口**
   - 地址: `GET /ai/love_app/chat/sse`
   - 参数: `message` (消息内容), `chatId` (聊天室ID)
   - 返回: SSE流式响应

2. **AI超级智能体接口**
   - 地址: `GET /ai/manus/chat`
   - 参数: `message` (消息内容)
   - 返回: SSE流式响应

## 使用说明

1. 访问主页选择要使用的AI应用
2. 在聊天页面输入问题或消息
3. AI会通过SSE实时返回响应
4. 支持多轮对话，每个聊天室有独立的会话ID

## 注意事项

- 确保后端服务已启动并运行在8123端口
- 浏览器需要支持SSE (Server-Sent Events)
- 建议使用现代浏览器以获得最佳体验
