import { buildUrl, streamGet } from '../utils/sse'

export const chatLoveStream = (message, chatId, handlers) =>
  streamGet(
    buildUrl('/ai/love_app/chat/sse', { message, chatId }),
    handlers
  )

export const chatManusStream = (message, handlers) =>
  streamGet(buildUrl('/ai/manus/chat', { message }), handlers)
