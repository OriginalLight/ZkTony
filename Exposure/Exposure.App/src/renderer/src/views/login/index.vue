<template>
  <div class="container">
    <div class="logo">
      <img alt="logo" src="../../assets/logo.png" style="height: 64px" />
    </div>
    <div class="content">
      <LoginForm />
    </div>
    <div class="qrcode">
      <img alt="qrcode" src="../../assets/qrcode.png" style="height: 96px" />
    </div>
  </div>
</template>

<script lang="ts" setup>
import LoginForm from './components/login-form.vue'
</script>

<style lang="less" scoped>
.container {
  height: 100vh;
  background-color: var(--color-bg-2);

  .content {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    display: flex;
    flex: 1;
    align-items: center;
    justify-content: center;
    padding-bottom: 40px;
  }
}

.logo {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
}

.qrcode {
  position: absolute;
  bottom: 16px;
  right: 16px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
}
</style>
