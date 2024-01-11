<template>
  <a-space class="space" :size="30">
    <template #split>
      <a-divider direction="vertical" />
    </template>
    <a-tooltip :content="$t('navigation.status.temperature')">
      <h3 style="white-space: nowrap">{{ status.temperature }}</h3>
    </a-tooltip>
    <a-tooltip :content="$t('navigation.status.usb')">
      <icon-usb size="24" :fill="status.usb" />
    </a-tooltip>
    <a-tooltip :content="$t('navigation.status.door')">
      <icon-open-door size="24" :fill="status.door" />
    </a-tooltip>
    <a-button class="logout" @click="visible = true">
      <template #icon>
        <icon-user />
      </template>
      {{ userStore.name }}
    </a-button>
  </a-space>
  <a-modal v-model:visible="visible" draggable @ok="handleLogout" @cancel="visible = false">
    <template #title> {{ t('navigation.status.logout.title') }} </template>
    <div>
      {{ t('navigation.status.logout.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, reactive, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useUserStore } from '@renderer/store'
import { Usb as IconUsb, OpenDoor as IconOpenDoor } from '@icon-park/vue-next'

// 国际化
const { t } = useI18n()
// 路由
const router = useRouter()
// 获取用户信息
const userStore = useUserStore()
// 注销弹窗
const visible = ref(false)
// 状态栏的一些图标颜色和文字
const status = reactive({
  // usb图标颜色
  usb: '#808080',
  // 舱门图标颜色
  door: '#808080',
  // 温度
  temperature: '0.0°C'
})
// websocket
const socket = new WebSocket(import.meta.env.RENDERER_VITE_METRIC_WEBSOCKET_URL)
// 确认注销
const handleLogout = async () => {
  try {
    visible.value = false
    await userStore.logout()
    router.push({ name: 'login' })
    Message.success(t('navigation.status.logout.success'))
  } catch (err) {
    Message.success(t('navigation.status.logout.failed'))
  }
}
// 定时发送websocket请求
const timer = setInterval(() => {
  socket.send('all')
}, 3000)
// websocket连接成功
socket.addEventListener('open', () => {
  console.log('websocket connected')
})
// websocket接收到消息
socket.addEventListener('message', (event) => {
  try {
    const json = JSON.parse(event.data)
    status.usb = json.usb === 0 ? '#808080' : '#1890ff'
    status.temperature = json.temperature <= -100 ? '/' : `${json.temperature} °C`
    status.door = json.door === 0 ? '#808080' : '#1890ff'
  } catch (err) {
    console.log(err)
  }
})
// websocket连接失败
socket.addEventListener('error', (err) => {
  console.log(err)
})
// 组件销毁时清除定时器
onBeforeUnmount(() => {
  console.log('websocket disconnected')
  socket.close()
  clearInterval(timer)
})
</script>

<style lang="less" scoped>
.status {
  float: right;
}
.space {
  vertical-align: middle;
  padding: 0 30px;
  color: var(--color-text-2);
}
.logout {
  max-width: 200px;
  overflow: hidden;
}
</style>
