<template>
  <div class="container">
    <gallery-bar @search="handleSearch" />
    <gallery-content ref="content" />
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import GalleryBar from './components/gallery-bar.vue'
import GalleryContent from './components/gallery-content.vue'
import useGalleryState from '@renderer/states/gallery'

const { selected, paginationProps } = useGalleryState()

const content = ref<InstanceType<typeof GalleryContent>>()

const handleSearch = () => {
  selected.value = []
  paginationProps.current = 1
  content.value?.fetchData()
}

// 选项
</script>

<style lang="less" scoped>
.container {
  display: flex;
  flex-direction: column;
  padding: 8px;
  margin: 8px;
  height: calc(100vh - 66px - 32px);
  border-radius: 4px;
  background: var(--color-bg-2);
  overflow: hidden;
}
</style>
