<template>
  <a-row class="container">
    <a-col :span="14" class="image-preview">
      <ImagePreview :image="preview" @adjust="handleAdjust" />
    </a-col>
    <a-col :span="10" class="operation">
      <CameraOptions @shoot="handleShoot" @preview="handlePreview" />
      <ThumbnailView
        :preview="preview"
        :thumbnail="thumbnail"
        :selected="selected"
        @selected="handleSelected"
        @combine="handleCombine"
      />
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import ImagePreview from './components/image-preview.vue'
import CameraOptions from './components/camera-options.vue'
import ThumbnailView from './components/thumbnail-view.vue'
import { Picture } from '@renderer/api/picture'
import useHomeState from '@renderer/states/home'
import { init } from '@renderer/api/camera'
import { storage } from '@renderer/api/machine'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()

const { preview, thumbnail, selected, isInit } = useHomeState()

// 拍摄
const handleShoot = (images: Picture[]) => {
  preview.value = images[0]
  thumbnail.value = images.concat(thumbnail.value)
  selected.value = []
}

// 预览
const handlePreview = (image: Picture) => {
  preview.value = image
  selected.value = []
}

// 选择
const handleSelected = (image: Picture) => {
  if (selected.value.find((item) => item.id === image.id)) {
    selected.value = selected.value.filter((item) => item.id !== image.id)
  } else {
    selected.value.push(image)
  }
  preview.value = image
}

// 合成
const handleCombine = (image: Picture) => {
  preview.value = image
  // image 放到thumbnail中
  thumbnail.value = thumbnail.value.concat(image)
  selected.value = []
}

// 调整
const handleAdjust = (image: Picture) => {
  preview.value = image
  thumbnail.value = thumbnail.value.concat(image)
  selected.value = []
}

// 初始化
onMounted(async () => {
  try {
    if (!isInit.value) {
      await init()
      const res = await storage()
      if (res.data < 0.1) {
        Message.error(t('home.storage.error'))
      }
      isInit.value = true
    }
  } catch (error) {
    Message.error((error as Error).message)
  }
})
</script>

<style lang="less" scoped>
.image-preview {
  position: relative;
  display: flex;
  flex-direction: column;
  padding: 4px;
  height: calc(100vh - 66px);
  overflow: hidden;
}

.operation {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  height: calc(100vh - 66px);
  padding-top: 4px;
  padding-right: 4px;
  padding-bottom: 4px;
}
</style>
./state/home-state ../../states/index
