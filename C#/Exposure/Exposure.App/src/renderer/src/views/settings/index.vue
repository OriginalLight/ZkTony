<template>
  <div class="container">
    <div class="card">
      <a-space direction="vertical" size="medium">
        <SystemSettings />
        <UserSettings />
        <FactortSettings />
      </a-space>
    </div>
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
  height: calc(100vh - 66px - 32px);
  padding: 8px;
  margin: 8px;
  border-radius: 4px;
  background: var(--color-bg-2);
  overflow: hidden;

  .card {
    display: flex;
    flex: 1;
    flex-direction: column;
    overflow-y: scroll;
  }
}
.card::-webkit-scrollbar {
  display: none;
}
</style>
