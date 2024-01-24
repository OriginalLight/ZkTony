<template>
  <div class="container">
    <a-grid :cols="1" :row-gap="16" class="grid-demo-grid" style="height: 100%">
      <a-grid-item class="demo-item">item</a-grid-item>
      <a-grid-item class="demo-item">item</a-grid-item>
      <a-grid-item class="demo-item">item</a-grid-item>
      <a-grid-item class="demo-item">item</a-grid-item>
      <a-grid-item class="demo-item">item</a-grid-item>
      <a-grid-item class="demo-item">item</a-grid-item>
    </a-grid>
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
</style>
