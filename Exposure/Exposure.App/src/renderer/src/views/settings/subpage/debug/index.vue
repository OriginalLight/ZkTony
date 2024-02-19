<template>
  <div class="container"></div>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import { led } from '@renderer/api/debug'

onMounted(() => {
  led({ code: 0 })
})
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 16px;
  height: calc(100vh - 66px - 32px);
}
</style>
