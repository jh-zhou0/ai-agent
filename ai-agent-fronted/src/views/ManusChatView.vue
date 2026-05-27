<script setup>
import { useRouter } from 'vue-router'
import ChatRoom from '../components/ChatRoom.vue'
import { useChat } from '../composables/useChat'
import { chatManusStream } from '../api/ai'

const router = useRouter()

const { messages, input, loading, send } = useChat({
  withChatId: false,
  streamCall: chatManusStream,
})

const goBack = () => router.push('/')
</script>

<template>
  <ChatRoom
    title="AI 超级智能体"
    subtitle="Manus 多步推理，按步骤实时推送结果"
    :messages="messages"
    :input="input"
    :loading="loading"
    @update:input="input = $event"
    @send="send"
    @back="goBack"
  />
</template>
