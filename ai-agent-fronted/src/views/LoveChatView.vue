<script setup>
import { useRouter } from 'vue-router'
import ChatRoom from '../components/ChatRoom.vue'
import { useChat } from '../composables/useChat'
import { chatLoveStream } from '../api/ai'

const router = useRouter()

const { chatId, messages, input, loading, send } = useChat({
  withChatId: true,
  streamCall: chatLoveStream,
})

const goBack = () => router.push('/')
</script>

<template>
  <ChatRoom
    title="AI 情感大师助手"
    subtitle="扮演恋爱心理专家，支持多轮会话"
    :chat-id="chatId"
    :messages="messages"
    :input="input"
    :loading="loading"
    @update:input="input = $event"
    @send="send"
    @back="goBack"
  />
</template>
