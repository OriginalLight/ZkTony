<template>
  <a-menu
    class="menu"
    mode="horizontal"
    :default-selected-keys="['/home']"
    @menu-item-click="handleMenuClick"
  >
    <a-menu-item
      v-for="item in routes"
      :key="item.path"
      class="menu-item"
      style="margin-right: 30px; font-size: 20px"
      :route="item"
    >
      {{ t(`${item.meta.locale}`) }}
    </a-menu-item>
  </a-menu>
</template>
<script lang="ts" setup>
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

// 获取所有路由并去除/和/login
const routes = router.getRoutes().filter((item) => item.meta?.nav)

// 点击导航栏跳转路由
const handleMenuClick = (e: string) => {
  router.push({
    path: e
  })
}
</script>

<style lang="less" scoped>
.menu {
  background-color: transparent;

  .menu-item {
    background-color: transparent;
    color: var(--color-bg-2);
  }
}
</style>
