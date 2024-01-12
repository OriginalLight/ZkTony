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
import { ref, reactive, onBeforeUnmount, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAppStore, useUserStore } from '@renderer/store'
import { Usb as IconUsb, OpenDoor as IconOpenDoor } from '@icon-park/vue-next'
import useSocket from '@renderer/hooks/socket'

// 国际化
const { t } = useI18n()
// 路由
const router = useRouter()
// 获取用户信息
const userStore = useUserStore()
// 获取应用信息
const appStore = useAppStore()
// websocket
const socket = useSocket()
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
  const message = JSON.stringify({ code: 'status' })
  socket.send(message)
}, 3000)

watch(socket.data, (data) => {
  if (data) {
    try {
      const obj: { code: string; data: never } = JSON.parse(data)
      if (obj.code === 'status') {
        const data: { usb: boolean; temperature: number; door: boolean } = obj.data
        status.usb = data.usb ? '#1890ff' : '#808080'
        status.temperature = data.temperature <= -100 ? '/' : `${data.temperature} °C`
        status.door = data.door ? '#1890ff' : '#808080'
        appStore.toggleUsb(data.usb)
        appStore.toggleDoor(data.door)
        appStore.toggleTemperature(data.temperature)
      }
    } catch (error) {
      console.log(error)
    }
  }
})

// 组件销毁时清除定时器
onBeforeUnmount(() => {
  console.log('clear timer')
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
