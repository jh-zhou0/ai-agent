const parseSseBlock = (block) => {
  const lines = block.split(/\r?\n/)
  const dataLines = []

  for (const line of lines) {
    if (line.startsWith('data:')) {
      dataLines.push(line.slice(5).replace(/^\s/, ''))
    }
  }

  return dataLines.length > 0 ? dataLines.join('\n') : block.trim()
}

export const buildUrl = (path, params = {}) => {
  const base = import.meta.env.VITE_API_BASE_URL
  const url = new URL(path.startsWith('http') ? path : `${base}${path}`)

  Object.entries(params).forEach(([key, value]) => {
    if (value != null && value !== '') {
      url.searchParams.set(key, value)
    }
  })

  return url.toString()
}

export const streamGet = async (url, { onChunk, onDone, onError, signal }) => {
  try {
    const response = await fetch(url, { signal })

    if (!response.ok) {
      throw new Error(`请求失败: HTTP ${response.status}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      buffer += decoder.decode(value, { stream: true })
      const blocks = buffer.split(/\r?\n\r?\n/)
      buffer = blocks.pop() ?? ''

      for (const block of blocks) {
        const text = parseSseBlock(block)
        if (text) onChunk(text)
      }
    }

    if (buffer.trim()) {
      const tail = parseSseBlock(buffer)
      if (tail) onChunk(tail)
    }

    onDone?.()
  } catch (error) {
    if (error?.name !== 'AbortError') {
      onError?.(error)
    }
  }
}
