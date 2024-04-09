<template>
  <a-space class="space" :size="10">
    <template #split>
      <a-divider direction="vertical" />
    </template>
    <a-tooltip :content="$t('navigation.status.temperature')">
      <h3 style="white-space: nowrap">
        {{ appStore.temperature <= -100 ? '#' : `${appStore.temperature} °C` }}
      </h3>
    </a-tooltip>
    <a-tooltip :content="$t('navigation.status.usb')">
      <usb size="24" :class="{ active: appStore.usb }" />
    </a-tooltip>
    <a-tooltip :content="$t('navigation.status.hatch')">
      <open-door size="24" :class="{ active: appStore.hatch }" />
    </a-tooltip>
    <a-tooltip :content="$t('navigation.status.time')">
      <div style="font-size: 10px">{{ now }}</div>
    </a-tooltip>
    <a-button class="logout" shape="round" @click="visible = true">
      <template #icon>
        <icon-user />
      </template>
      <div class="name">{{ userStore.name }}</div>
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
import { ref, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { Message } from '@arco-design/web-vue'
import { useAppStore, useUserStore } from '@renderer/store'
import { Usb, OpenDoor } from '@icon-park/vue-next'
import { useNow, useDateFormat } from '@vueuse/core'
import { metric } from '@renderer/api/metric'

// 国际化
const { t } = useI18n()
// 路由
const router = useRouter()
// 获取用户信息
const userStore = useUserStore()
// 获取应用信息
const appStore = useAppStore()
// 注销弹窗
const visible = ref(false)
// 时间
const now = useDateFormat(useNow(), 'HH:mm:ss YYYY/MM/DD')
// 确认注销
const handleLogout = async () => {
  try {
    visible.value = false
    await userStore.logout()
    router.push('/login')
    Message.success(t('navigation.status.logout.success'))
  } catch (err) {
    Message.error((err as Error).message)
  }
}
// 定时发送websocket请求
const timer = setInterval(async () => {
  try {
    const res = await metric()
    const data = res.data
    appStore.toggleUsb(data.usb)
    appStore.toggleHatch(data.hatch)
    appStore.toggleTemperature(data.temperature)
  } catch (err) {
    console.log(err)
  }
}, 3000)

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
  color: var(--color-bg-2);
}
.logout {
  max-width: 200px;
  min-width: 100px;
  overflow: hidden;

  .name {
    max-width: 150px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.active {
  color: rgb(var(--primary-6));
}
</style>
