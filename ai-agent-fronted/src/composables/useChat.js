import { ref, onMounted } from 'vue'

const createChatId = () =>
  crypto.randomUUID?.() ??
  `chat-${Date.now()}-${Math.random().toString(36).slice(2, 10)}`

const createMessageId = () =>
  `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`

export const useChat = ({ streamCall, withChatId = false }) => {
  const chatId = ref('')
  const messages = ref([])
  const input = ref('')
  const loading = ref(false)

  let abortController = null

  onMounted(() => {
    if (withChatId) {
      chatId.value = createChatId()
    }
  })

  const appendMessage = (role, content) => {
    messages.value.push({
      id: createMessageId(),
      role,
      content,
    })
  }

  const appendAssistantChunk = (chunk) => {
    const list = messages.value
    const last = list[list.length - 1]

    if (last?.role === 'assistant') {
      last.content += chunk
    } else {
      appendMessage('assistant', chunk)
    }
  }

  const send = async () => {
    const text = input.value.trim()
    if (!text || loading.value) return

    appendMessage('user', text)
    input.value = ''
    loading.value = true

    abortController?.abort()
    abortController = new AbortController()

    appendMessage('assistant', '')

    const handlers = {
      signal: abortController.signal,
      onChunk: appendAssistantChunk,
      onDone: () => {
        loading.value = false
      },
      onError: (error) => {
        loading.value = false
        appendAssistantChunk(`\n[错误] ${error?.message ?? '未知错误'}`)
      },
    }

    if (withChatId) {
      await streamCall(text, chatId.value, handlers)
    } else {
      await streamCall(text, handlers)
    }
  }

  return {
    chatId,
    messages,
    input,
    loading,
    send,
  }
}
