<template>
  <div v-if="show" class="container">
    <SystemMenu />
    <SystemStatus />
  </div>
  <a-divider :margin="0" />
</template>
<script lang="ts" setup>
import { ref, watch } from 'vue'
import { useRoute } from 'vue-router'
import SystemMenu from './components/menu.vue'
import SystemStatus from './components/status.vue'

const route = useRoute()
// 根据路由判断是否显示导航栏
const show = ref(false)
// 监听路由变化
watch(
  () => route.path,
  () => {
    show.value = route.path !== '/login'
  }
)
</script>

<style lang="less" scoped>
.container {
  display: inline-flex;
  box-sizing: border-box;
  width: 100%;
  height: 66px;
  vertical-align: middle;
}
</style>
