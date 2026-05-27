<script setup>
const apps = [
  {
    path: '/love',
    title: 'AI 情感大师助手',
    desc: '恋爱心理专家，支持多轮会话记忆，SSE 流式对话',
    icon: '💬',
  },
  {
    path: '/manus',
    title: 'AI 超级智能体',
    desc: 'Manus 多步推理智能体，按步骤实时推送执行结果',
    icon: '🤖',
  },
]
</script>

<template>
  <div class="home">
    <header class="home-header">
      <h1>AI 应用中心</h1>
      <p>选择下方应用开始体验</p>
    </header>

    <div class="app-grid">
      <router-link
        v-for="app in apps"
        :key="app.path"
        :to="app.path"
        class="app-card"
      >
        <span class="app-icon">{{ app.icon }}</span>
        <h2>{{ app.title }}</h2>
        <p>{{ app.desc }}</p>
        <span class="enter">进入应用 →</span>
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.home {
  min-height: 100vh;
  padding: 48px 24px;
  max-width: 900px;
  margin: 0 auto;
}

.home-header {
  text-align: center;
  margin-bottom: 40px;
}

.home-header h1 {
  margin: 0 0 8px;
  font-size: 2rem;
  color: #2e2e3a;
}

.home-header p {
  margin: 0;
  color: #666;
}

.app-grid {
  display: grid;
  gap: 20px;
}

@media (min-width: 640px) {
  .app-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.app-card {
  display: block;
  padding: 28px 24px;
  background: #fff;
  border-radius: 12px;
  text-decoration: none;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition:
    transform 0.15s ease,
    box-shadow 0.15s ease;
}

.app-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(108, 92, 231, 0.15);
}

.app-icon {
  font-size: 2rem;
  display: block;
  margin-bottom: 12px;
}

.app-card h2 {
  margin: 0 0 8px;
  font-size: 1.2rem;
  color: #2e2e3a;
}

.app-card p {
  margin: 0 0 16px;
  font-size: 0.9rem;
  color: #666;
  line-height: 1.5;
}

.enter {
  font-size: 0.9rem;
  color: #6c5ce7;
  font-weight: 500;
}
</style>
