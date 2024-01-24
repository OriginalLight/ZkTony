import { useWebSocket } from '@vueuse/core'

const url = import.meta.env.RENDERER_VITE_WEBSOCKET_URL
const { send, data } = useWebSocket(url, {
  autoReconnect: true
})

export default function useSocket() {
  return {
    send,
    data
  }
}
