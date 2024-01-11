<template>
  <div class="container">
    <a-space size="large" direction="vertical">
      <SystemSettings />
      <UserSettings />
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import SystemSettings from './components/system-settings.vue'
import UserSettings from './components/user-settings.vue'
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: calc(100vh - 98px);
  background-color: var(--color-bg-2);
}
// 隐藏滚动
</style>
