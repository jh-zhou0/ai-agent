<script setup>
import { ref, watch, nextTick } from 'vue'

const props = defineProps({
  title: { type: String, required: true },
  subtitle: { type: String, default: '' },
  chatId: { type: String, default: '' },
  messages: { type: Array, default: () => [] },
  input: { type: String, default: '' },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['update:input', 'send', 'back'])

const chatBodyRef = ref(null)

const scrollToBottom = async () => {
  await nextTick()
  const el = chatBodyRef.value
  if (el) el.scrollTop = el.scrollHeight
}

watch(
  () => props.messages,
  () => scrollToBottom(),
  { deep: true }
)
</script>

<template>
  <div class="chat-page">
    <header class="chat-header">
      <button type="button" class="back-btn" @click="emit('back')">← 返回</button>
      <div class="header-text">
        <h1>{{ title }}</h1>
        <p v-if="subtitle" class="subtitle">{{ subtitle }}</p>
        <p v-if="chatId" class="chat-id">会话 ID：{{ chatId }}</p>
      </div>
    </header>

    <main ref="chatBodyRef" class="chat-body">
      <div v-if="messages.length === 0" class="empty-hint">
        发送一条消息开始对话
      </div>
      <div
        v-for="msg in messages"
        :key="msg.id"
        class="bubble-row"
        :class="msg.role === 'user' ? 'is-user' : 'is-ai'"
      >
        <div class="avatar">{{ msg.role === 'user' ? '我' : 'AI' }}</div>
        <div class="bubble">{{ msg.content || (loading ? '…' : '') }}</div>
      </div>
    </main>

    <footer class="chat-footer">
      <input
        :value="input"
        type="text"
        placeholder="输入消息，Enter 发送"
        :disabled="loading"
        @input="emit('update:input', $event.target.value)"
        @keyup.enter="emit('send')"
      />
      <button type="button" class="send-btn" :disabled="loading" @click="emit('send')">
        {{ loading ? '生成中…' : '发送' }}
      </button>
    </footer>
  </div>
</template>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  max-width: 960px;
  margin: 0 auto;
  background: #ededed;
}

.chat-header {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 14px 16px;
  background: #2e2e3a;
  color: #fff;
}

.back-btn {
  flex-shrink: 0;
  padding: 6px 10px;
  border: none;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.back-btn:hover {
  background: rgba(255, 255, 255, 0.2);
}

.header-text h1 {
  margin: 0;
  font-size: 1.15rem;
  font-weight: 600;
}

.subtitle {
  margin: 4px 0 0;
  font-size: 0.85rem;
  opacity: 0.85;
}

.chat-id {
  margin: 6px 0 0;
  font-size: 0.75rem;
  opacity: 0.65;
  word-break: break-all;
}

.chat-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}

.empty-hint {
  text-align: center;
  color: #888;
  margin-top: 40px;
  font-size: 0.9rem;
}

.bubble-row {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-bottom: 14px;
}

.bubble-row.is-user {
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 600;
  color: #fff;
}

.is-ai .avatar {
  background: #6c5ce7;
}

.is-user .avatar {
  background: #00b894;
}

.bubble {
  max-width: 72%;
  padding: 10px 14px;
  border-radius: 8px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.5;
  font-size: 0.95rem;
}

.is-ai .bubble {
  background: #fff;
  border: 1px solid #e0e0e0;
  border-top-left-radius: 2px;
}

.is-user .bubble {
  background: #95ec69;
  border-top-right-radius: 2px;
}

.chat-footer {
  display: flex;
  gap: 10px;
  padding: 12px 16px;
  background: #f7f7f7;
  border-top: 1px solid #ddd;
}

.chat-footer input {
  flex: 1;
  padding: 10px 14px;
  border: 1px solid #ccc;
  border-radius: 6px;
  outline: none;
}

.chat-footer input:focus {
  border-color: #6c5ce7;
}

.send-btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  background: #6c5ce7;
  color: #fff;
  font-weight: 500;
}

.send-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.send-btn:not(:disabled):hover {
  background: #5b4cdb;
}
</style>
