# AI Agent 前端

Vue 3 + Vite + Vue Router + Axios 实现的 AI 应用中心，包含情感大师与 Manus 超级智能体两个聊天应用。

## 功能

- **主页**：切换进入不同 AI 应用
- **情感大师**：进入页面自动生成 `chatId`，通过 SSE 调用 `/ai/love_app/chat/sse`
- **超级智能体**：聊天室 UI 相同，通过 SSE 调用 `/ai/manus/chat`

## 启动

```bash
# 安装依赖
npm install

# 开发（默认 http://localhost:5173）
npm run dev

# 构建
npm run build
```

请确保后端已启动：`http://localhost:8123/api`

## 环境变量

`.env.development`：

```
VITE_API_BASE_URL=http://localhost:8123/api
```

开发时也可使用 Vite 代理（`vite.config.js` 中 `/api` → `8123`），此时可将 `VITE_API_BASE_URL` 改为 `/api`。

## 接口说明

| 应用     | 方法 | 路径                         | 参数                    |
|----------|------|------------------------------|-------------------------|
| 情感大师 | GET  | `/ai/love_app/chat/sse`      | `message`, `chatId`     |
| 智能体   | GET  | `/ai/manus/chat`             | `message`               |

SSE 使用 `fetch` + 流式解析；Axios 封装在 `src/api/request.js`，供后续普通 HTTP 接口使用。
