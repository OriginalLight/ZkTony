<template>
  <div v-dragscroll class="container">
    <a-space size="large" direction="vertical">
      <SystemSettings />
      <UserSettings />
      <FactortSettings />
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import SystemSettings from './components/system-settings.vue'
import UserSettings from './components/user-settings.vue'
import FactortSettings from './components/factory-settings.vue'
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: calc(100vh - 98px);
  overflow-y: scroll;
}
.container::-webkit-scrollbar {
  display: none;
}
</style>
