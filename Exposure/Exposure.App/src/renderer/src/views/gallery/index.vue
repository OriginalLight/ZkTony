<template>
  <div class="container">
    <h1>Gallery</h1>
    <a-slider :default-value="50" :style="{ width: '200px' }" />
  </div>
</template>

<script lang="ts" setup></script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 66px);
  background-color: var(--color-bg-2);
}
// 隐藏滚动
</style>
