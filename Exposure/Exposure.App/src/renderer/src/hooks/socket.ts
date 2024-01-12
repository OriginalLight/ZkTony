import { useWebSocket } from '@vueuse/core'

export default function useSocket() {
  const url = import.meta.env.RENDERER_VITE_WEBSOCKET_URL
  const { status, send, data } = useWebSocket(url, {
    autoReconnect: true
  })

  return {
    status,
    send,
    data
  }
}
