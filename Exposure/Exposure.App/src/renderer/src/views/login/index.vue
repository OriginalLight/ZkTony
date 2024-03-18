<template>
  <div class="container">
    <div class="logo">
      <img alt="logo" src="../../assets/logo.png" style="height: 64px" />
    </div>
    <div class="content">
      <LoginForm />
    </div>
    <a-space class="qrcode">
      <a-space direction="vertical" :size="4">
        <img alt="qrcode" src="../../assets/qrcode.png" style="height: 96px" />
        <div style="font-size: 12px; width: 96px; text-align: center">
          {{ t('login.qrcode.0') }}
        </div>
      </a-space>

      <a-space direction="vertical" :size="4">
        <img alt="qrcode" src="../../assets/qrcode1.png" style="height: 96px" />
        <div style="font-size: 12px; width: 96px; text-align: center">
          {{ t('login.qrcode.1') }}
        </div>
      </a-space>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import { useI18n } from 'vue-i18n'
import LoginForm from './components/login-form.vue'

const { t } = useI18n()
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
