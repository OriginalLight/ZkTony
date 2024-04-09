<template>
  <div class="layout">
    <div class="layout-header"><Navigation /></div>
    <div class="layout-content">
      <a-config-provider :locale="locale">
        <router-view v-slot="{ Component, route }">
          <keep-alive v-if="route.meta.keepAlive">
            <component :is="Component" />
          </keep-alive>
          <component :is="Component" v-else />
        </router-view>
      </a-config-provider>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { computed, onMounted } from 'vue'
import enUS from '@arco-design/web-vue/es/locale/lang/en-us'
import zhCN from '@arco-design/web-vue/es/locale/lang/zh-cn'
import Navigation from '@renderer/components/navigation/index.vue'
import useLocale from '@renderer/hooks/locale'
import useTheme from '@renderer/hooks/themes'

const { currentLocale } = useLocale()
const { initTheme } = useTheme()
const locale = computed(() => {
  switch (currentLocale.value) {
    case 'zh-CN':
      return zhCN
    case 'en-US':
      return enUS
    default:
      return enUS
  }
})

onMounted(() => {
  initTheme()
})
</script>

<style lang="less" scoped>
.layout {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(to bottom right, #049a83, #1a6eb6);

  .layout-content {
    overflow: hidden;
    flex: 1;
  }
}
</style>
