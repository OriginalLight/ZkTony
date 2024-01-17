<template>
  <a-layout class="layout">
    <a-layout-header class="layout-header"> <Navigation /> </a-layout-header>
    <a-layout-content class="layout-content">
      <a-config-provider :locale="locale">
        <router-view />
      </a-config-provider>
    </a-layout-content>
  </a-layout>
</template>

<script lang="ts" setup>
import { computed } from 'vue'
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
// 初始化主题
initTheme()
</script>

<style lang="less" scoped>
.layout {
  height: 100vh;
  background-color: var(--color-bg-2);
  .layout-header {
    height: 66px;
  }
  .layout-content {
    overflow: hidden;
    height: calc(100vh - 66px);
  }
}

::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 0;
}

::-webkit-scrollbar {
  -webkit-appearance: none;
  width: 6px;
  height: 6px;
}

::-webkit-scrollbar-thumb {
  cursor: pointer;
  border-radius: 5px;
  background: rgba(0, 0, 0, 0.15);
  transition: color 0.2s ease;
}
</style>
