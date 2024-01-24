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
        :thumbnail-selected="thumbnailSelected"
        @selected="handleSelected"
        @combine="handleCombine"
      />
    </a-col>
  </a-row>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import dayjs from 'dayjs'
import ImagePreview from './components/image-preview.vue'
import CameraOptions from './components/camera-options.vue'
import ThumbnailView from './components/thumbnail-view.vue'
import { Picture, getByPage } from '@renderer/api/picture'

// 预览
const preview = ref<Picture>({
  id: 0,
  userId: 0,
  name: 'None',
  path: '',
  width: 1000,
  height: 1000,
  type: 0,
  exposureTime: 0,
  exposureGain: 0,
  blackLevel: 0,
  isDelete: false,
  createTime: '',
  updateTime: '',
  deleteTime: ''
})

// 缩略图
const thumbnail = ref<Picture[]>([])

// 缩略图选中
const thumbnailSelected = ref<Picture[]>([])

// 拍摄
const handleShoot = (images: Picture[]) => {
  preview.value = images[0]
  thumbnail.value = images.concat(thumbnail.value)
  thumbnailSelected.value = []
}

// 预览
const handlePreview = (image: Picture) => {
  preview.value = image
  thumbnailSelected.value = []
}

// 选择
const handleSelected = (image: Picture) => {
  if (thumbnailSelected.value.find((item) => item.id === image.id)) {
    thumbnailSelected.value = thumbnailSelected.value.filter((item) => item.id !== image.id)
  } else {
    thumbnailSelected.value.push(image)
  }
  preview.value = image
}

// 合成
const handleCombine = (image: Picture) => {
  preview.value = image
  // image 放到thumbnail中
  thumbnail.value = thumbnail.value.concat(image)
  thumbnailSelected.value = []
}

// 调整
const handleAdjust = (image: Picture) => {
  preview.value = image
  thumbnail.value = thumbnail.value.concat(image)
  thumbnailSelected.value = []
}

// 获取当天的图片
const fetchPicture = async () => {
  try {
    const res = await getByPage({
      page: 1,
      size: 1000,
      name: '',
      isDeleted: false,
      startTime: dayjs(new Date()).format('YYYY-MM-DD'),
      endTime: dayjs(new Date()).add(1, 'day').format('YYYY-MM-DD')
    })
    if (res.data.list.length > 0) {
      preview.value = res.data.list[0]
      thumbnail.value = res.data.list
    }
  } catch (error) {
    console.log(error)
  }
}

fetchPicture()
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
