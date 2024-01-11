<template>
  <a-row class="container">
    <a-col :span="12">
      <div class="left">
        <a-image
          src="C:\Users\ThinkBook\Desktop\test\2024-01-10-14-33-15.jpg"
          fit="scale-down"
          width="100%"
          height="100%"
        />
      </div>
    </a-col>

    <a-col :span="12">
      <div class="right">
        <a-divider :margin="0" direction="vertical" style="height: 100%" />
      </div>
    </a-col>
  </a-row>
</template>

<script lang="ts" setup></script>

<style lang="less" scoped>
.left {
  display: flex;
  align-items: center;
  flex-direction: column;
  padding: 8px;
  height: calc(100vh - 66px - 16px);
  overflow: hidden;
  justify-content: center;
}

.right {
  display: flex;
  overflow: hidden;
  height: calc(100vh - 66px);
}
// 隐藏滚动
</style>
