<template>
  <div class="box">
    <div class="preview-box">
      <a-button
        class="button"
        type="primary"
        style="width: 100px"
        :disabled="diableCombine"
        :loading="loading.combine"
        @click="handleCombine"
      >
        <template #icon>
          <icon-export />
        </template>
        {{ t('home.album.view.combine') }}</a-button
      >
      <a-button
        class="button"
        type="primary"
        status="danger"
        style="width: 100px"
        :disabled="subpage.selected.length === 0"
        :loading="loading.delete"
        @click="visible.delete = true"
      >
        <template #icon>
          <icon-delete />
        </template>
        {{ t('gallery.bar.delete') }}</a-button
      >
      <a-space direction="vertical" class="preview">
        <div
          v-for="(item, index) in subpage.album.photos"
          :key="item.id"
          class="img-box"
          @click="handelSelected(item)"
        >
          <img v-lazy="item.thumbnail" class="img" />
          <div v-if="showSelected(item)" class="selected">√</div>
          <div v-if="showPreview(item)" class="img-preview">
            {{ t('home.album.view.preview') }}
          </div>
          <div class="img-index">{{ index + 1 }}</div>
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
    <a-divider direction="vertical" style="height: 100%" :margin="8" />
    <div class="col">
      <div class="col-1">
        <div class="img-preview-box">
          <img
            id="img-preview"
            v-lazy="subpage.preview?.path"
            class="img"
            :style="{
              filter: `brightness(${options.brightness}%) contrast(${options.contrast}%) invert(${options.invert ? 100 : 0}%)`,
              transform: `rotate(${options.rotate}deg) scale(${options.scale})`
            }"
          />
        </div>
        <a-space class="tools" size="medium">
          <a-tooltip placement="top" :content="t('gallery.detail.3dchart')">
            <a-button type="primary" size="medium" @click="handle3dChart">
              <template #icon>
                <CoordinateSystem />
              </template>
            </a-button>
          </a-tooltip>
          <a-tooltip placement="top" :content="t('gallery.detail.redo')">
            <a-button type="primary" size="medium" :disabled="disabledRedo" @click="resetOptions">
              <template #icon>
                <Refresh />
              </template>
            </a-button>
          </a-tooltip>
          <a-tooltip placement="top" :content="t('gallery.detail.save')">
            <a-button
              type="primary"
              size="medium"
              :disabled="options.brightness === 100 && options.contrast === 100 && !options.invert"
              :loading="loading.save"
              @click="handleSave"
            >
              <template #icon>
                <Save />
              </template>
            </a-button>
          </a-tooltip>
        </a-space>
      </div>
      <a-divider direction="vertical" style="height: 100%" :margin="8" />
      <div class="col-2">
        <a-space direction="vertical" style="width: 100%">
          <a-input
            v-model="subpage.preview.name"
            allow-clear
            :max-length="32"
            @input="handleUpdate()"
          >
            <template #prepend> {{ t('gallery.detail.name') }} </template>
          </a-input>
          <a-input :model-value="subpage.preview.width + ' x ' + subpage.preview.height" readonly>
            <template #prepend> {{ t('gallery.detail.size') }} </template>
          </a-input>
          <a-input :model-value="getExposureTime(subpage.preview.exposureTime)" readonly>
            <template #prepend> {{ t('gallery.detail.expo') }} </template>
            <template #append>
              {{ t('gallery.detail.unit') }}
            </template>
          </a-input>
          <a-input :model-value="subpage.album.user?.name" readonly>
            <template #prepend> {{ t('gallery.detail.user') }} </template>
          </a-input>
          <a-input :model-value="subpage.preview.createTime" readonly>
            <template #prepend> {{ t('gallery.detail.time') }} </template>
          </a-input>
          <a-divider margin="0" />
          <a-space direction="vertical" size="medium" style="width: 100%">
            <a-button-group style="width: 100%">
              <a-button style="width: 100%" @click="options.rotate -= 90">
                <template #icon>
                  <icon-rotate-left />
                </template>
                {{ t('gallery.detail.rotate.left') }}
              </a-button>
              <a-button style="width: 100%" @click="options.rotate += 90">
                <template #icon>
                  <icon-rotate-right />
                </template>
                {{ t('gallery.detail.rotate.right') }}
              </a-button>
            </a-button-group>
            <div class="op">
              <a-tag>{{ t('gallery.detail.invert') }}</a-tag>
              <a-switch
                v-model="options.invert"
                style="margin-left: 16px"
                @change="handleRefreshHistogram"
              />
            </div>

            <div class="op">
              <a-tag>{{ t('gallery.detail.brightness') }}</a-tag>
              <Slider
                v-model="options.brightness"
                class="slider"
                :max="300"
                :lazy="false"
                show-tooltip="drag"
                @change="handleRefreshHistogram"
              />
              <a-tag>{{ options.brightness }}</a-tag>
            </div>

            <div class="op">
              <a-tag>{{ t('gallery.detail.contrast') }}</a-tag>
              <Slider
                v-model="options.contrast"
                class="slider"
                :max="300"
                :lazy="false"
                show-tooltip="drag"
                @change="handleRefreshHistogram"
              />
              <a-tag>{{ options.contrast }}</a-tag>
            </div>
          </a-space>
        </a-space>
        <canvas id="canvas"></canvas>
      </div>
    </div>
  </div>
  <a-modal
    v-model:visible="visible.delete"
    draggable
    @ok="handleDelete"
    @cancel="visible.delete = false"
  >
    <template #title> {{ t('gallery.bar.delete.title') }} </template>
    <div>
      {{ t('gallery.bar.delete.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, onMounted, computed } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { CoordinateSystem, Refresh, Save } from '@icon-park/vue-next'
import { Message } from '@arco-design/web-vue'
import useGalleryState from '@renderer/states/gallery'
import { useWindowSize } from '@vueuse/core'
import Slider from '@vueform/slider'
import {
  Photo,
  adjustPhoto,
  combinePhoto,
  deleteAlbum,
  deletePhoto,
  updatePhoto
} from '@renderer/api/album'

const { subpage } = useGalleryState()

const { t } = useI18n()
const router = useRouter()
// 窗口高度
const { height } = useWindowSize()

const options = ref({
  brightness: 100,
  contrast: 100,
  grayScale: 0,
  rotate: 0,
  invert: false,
  scale: 1.2,
  offsetX: 0,
  offsetY: 0
})

const loading = ref({
  save: false,
  delete: false,
  combine: false
})

const visible = ref({
  delete: false
})

// 是否禁用合并
const diableCombine = computed(() => {
  // 数量等于2, 类型一个是白光一个是曝光, 不能同时是白光或者曝光
  return (
    subpage.value.selected.length !== 2 ||
    !(
      subpage.value.selected.some((item) => item.type === 0) &&
      subpage.value.selected.some((item) => item.type === 1)
    )
  )
})

const resetOptions = () => {
  options.value = {
    brightness: 100,
    contrast: 100,
    invert: false,
    grayScale: 0,
    rotate: 0,
    scale: 1.2,
    offsetX: 0,
    offsetY: 0
  }
  const img = document.getElementById('img-preview') as HTMLImageElement
  img.style.left = '0px'
  img.style.top = '0px'

  handleHistogram(subpage.value.preview.thumbnail)
}

const disabledRedo = computed(() => {
  return (
    options.value.brightness === 100 &&
    options.value.contrast === 100 &&
    !options.value.invert &&
    options.value.rotate === 0 &&
    options.value.scale === 1.2 &&
    options.value.offsetX === 0 &&
    options.value.offsetY === 0
  )
})

const showSelected = (img: Photo) => {
  return subpage.value.selected.some((p) => p.id === img.id)
}

const showPreview = (img: Photo) => {
  return subpage.value.preview.id === img.id
}

// 删除
const handleDelete = async () => {
  visible.value.delete = false
  try {
    loading.value.delete = true
    const ids: number[] = subpage.value.selected.map((item) => item.id)
    await deletePhoto(ids)
    // 如果删除了所有的图片, 则返回上一页
    if (subpage.value.album.photos.length === ids.length) {
      router.push('/gallery')
      await deleteAlbum([subpage.value.album.id])
    }
    subpage.value.selected = []
    subpage.value.album.photos = subpage.value.album.photos.filter((item) => !ids.includes(item.id))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.delete = false
  }
}

// 合并
const handleCombine = async () => {
  try {
    loading.value.combine = true
    const res = await combinePhoto(subpage.value.selected.map((item) => item.id))
    if (res.data) {
      subpage.value.preview = res.data
      subpage.value.selected = []
      const photos = subpage.value.album.photos.concat(res.data)
      subpage.value.album.photos = photos
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.combine = false
  }
}

const handle3dChart = async () => {
  router.push('/gallery-chart')
}

const handelSelected = async (img: Photo) => {
  subpage.value.preview = img
  const photos = subpage.value.selected
  if (photos.some((p) => p.id === img.id)) {
    subpage.value.selected = photos.filter((p) => p.id != img.id)
  } else {
    subpage.value.selected = photos.concat(img)
  }
  resetOptions()
  handleHistogram(img.thumbnail)
}

const handleUpdate = async () => {
  try {
    await updatePhoto({
      id: subpage.value.preview.id,
      name: subpage.value.preview.name
    })
  } catch (error) {
    console.error(error)
  }
}

const handleSave = async () => {
  try {
    loading.value.save = true
    const res = await adjustPhoto({
      id: subpage.value.preview.id,
      brightness: options.value.brightness,
      contrast: options.value.contrast,
      invert: options.value.invert,
      code: 0
    })
    if (res.data) {
      subpage.value.preview = res.data
      const photos = subpage.value.album.photos.concat(res.data)
      subpage.value.album.photos = photos
      resetOptions()
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.save = false
  }
}

const getExposureTime = (time) => {
  // 返回 分钟：秒：毫秒
  const min = Math.floor(time / 60000)
  const sec = Math.floor((time % 60000) / 1000)
  const ms = Math.floor(time % 1000)

  // 使用 padStart 来确保格式正确
  const formattedMin = String(min).padStart(2, '0')
  const formattedSec = String(sec).padStart(2, '0')
  const formattedMs = String(ms).padStart(3, '0')

  return `${formattedMin} : ${formattedSec} : ${formattedMs}`
}

const handleHistogram = (src: string) => {
  const canvas = document.getElementById('canvas') as HTMLCanvasElement
  const ctx = canvas.getContext('2d') as CanvasRenderingContext2D
  const img = new Image()

  img.onload = () => {
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0, img.width, img.height)
    const originalImageData = ctx.getImageData(0, 0, img.width, img.height)
    const grayList = new Array(256)

    for (let i = 0; i < grayList.length; i++) {
      grayList[i] = 0
    }

    let gray = 0
    for (let i = 0; i < originalImageData.data.length; i += 4) {
      gray = Math.round(
        (originalImageData.data[i + 0] +
          originalImageData.data[i + 1] +
          originalImageData.data[i + 2]) /
          3
      )

      grayList[gray] += 1 // 统计灰度值数量
    }

    const hei = height.value * 0.4

    canvas.width = 256 * 2 + 10
    canvas.height = hei + 10
    // 缩放比例

    const scale = 20
    const scaleHeight = hei / (Math.max(...grayList) / scale)
    ctx.lineWidth = 1

    // 绘制Y坐标轴
    ctx.beginPath()
    ctx.moveTo(0, 0)
    ctx.lineTo(0, canvas.height)
    ctx.stroke()

    // 绘制X坐标轴
    ctx.beginPath()
    ctx.moveTo(0, canvas.height)
    ctx.lineTo(canvas.width, canvas.height)
    ctx.stroke()

    // 绘制x轴刻度
    for (let i = 0; i < 6; i++) {
      ctx.beginPath()
      ctx.moveTo(i * 100 + 10, canvas.height)
      ctx.lineTo(i * 100 + 10, canvas.height - 5)
      ctx.stroke()
    }

    // 绘制y轴刻度
    for (let i = 0; i < 6; i++) {
      ctx.beginPath()
      ctx.moveTo(0, (i * (hei + 10)) / 5 - 10)
      ctx.lineTo(5, (i * (hei + 10)) / 5 - 10)
      ctx.stroke()
    }

    // 左上角坐标轴开始绘制
    for (let i = 0; i < grayList.length; i++) {
      ctx.beginPath()
      ctx.moveTo(
        i * 2 + 10,
        canvas.height - (grayList[i] * scaleHeight > 360 ? 360 : grayList[i] * scaleHeight) - 10
      )
      ctx.lineTo(i * 2 + 10, canvas.height - 10)
      ctx.stroke()
    }
  }

  img.src = src
}

const handleRefreshHistogram = async () => {
  try {
    const res = await adjustPhoto({
      id: subpage.value.preview.id,
      brightness: options.value.brightness,
      contrast: options.value.contrast,
      invert: options.value.invert,
      code: 1
    })
    if (res.data) {
      handleHistogram(res.data.thumbnail)
    }
  } catch (error) {
    console.error(error)
  }
}

const handleTouch = () => {
  // 双指缩放
  const div = document.querySelector('.img-preview-box') as HTMLDivElement
  let startX, startY
  let distanceStart, distanceEnd

  div.addEventListener('touchstart', (e) => {
    if (e.touches.length === 2) {
      startX = Math.abs(e.touches[0].pageX - e.touches[1].pageX)
      startY = Math.abs(e.touches[0].pageY - e.touches[1].pageY)
      distanceStart = Math.sqrt(startX * startX + startY * startY) // 计算两个触点之间的距离
    }
  })

  div.addEventListener('touchmove', (e) => {
    if (e.touches.length === 2) {
      const endX = Math.abs(e.touches[0].pageX - e.touches[1].pageX)
      const endY = Math.abs(e.touches[0].pageY - e.touches[1].pageY)
      if (Math.abs(startX - endX) < 10 && Math.abs(startY - endY) < 10) {
        return
      }
      distanceEnd = Math.sqrt(endX * endX + endY * endY) // 计算两个触点之间的距离
      const scale = distanceEnd / distanceStart // 计算缩放比例
      // 根据缩放比例来缩放图片或其他内容
      options.value.scale = scale
    }
  })

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  div.addEventListener('touchend', (_e) => {
    startX = startY = distanceStart = distanceEnd = 0
  })

  // 鼠标滚轮缩放
  div.addEventListener('wheel', (e) => {
    if (e.deltaY < 0) {
      options.value.scale += 0.1
    } else {
      options.value.scale -= 0.1
    }
  })

  //img可以拖动
  const img = document.getElementById('img-preview') as HTMLImageElement
  let isDown = false
  let disX, disY
  img.addEventListener('mousedown', (e) => {
    isDown = true
    disX = e.clientX - img.offsetLeft
    disY = e.clientY - img.offsetTop
  })
  img.addEventListener('mousemove', (e) => {
    if (isDown) {
      img.style.left = e.clientX - disX + 'px'
      img.style.top = e.clientY - disY + 'px'
      options.value.offsetX = e.clientX - disX
      options.value.offsetY = e.clientY - disY
    }
  })
  img.addEventListener('mouseup', () => {
    isDown = false
  })
  //触屏拖动
  img.addEventListener('touchstart', (e) => {
    isDown = true
    disX = e.touches[0].clientX - img.offsetLeft
    disY = e.touches[0].clientY - img.offsetTop
  })
  img.addEventListener('touchmove', (e) => {
    if (isDown) {
      img.style.left = e.touches[0].clientX - disX + 'px'
      img.style.top = e.touches[0].clientY - disY + 'px'
      options.value.offsetX = e.touches[0].clientX - disX
      options.value.offsetY = e.touches[0].clientY - disY
    }
  })
  img.addEventListener('touchend', () => {
    isDown = false
  })
}

onMounted(() => {
  handleHistogram(subpage.value.preview.thumbnail)
  handleTouch()
})
</script>

<style scoped>
.box {
  display: flex;
  flex-direction: row;
  padding: 8px;
  margin: 8px;
  border-radius: 4px;
  height: calc(100vh - 66px - 32px);
  background: var(--color-bg-2);
}

.preview-box {
  display: flex;
  flex-direction: column;
  align-items: center;

  .button {
    margin-bottom: 8px;
  }
}

.preview {
  display: flex;
  flex: 1;
  overflow-x: hidden;
  overflow-y: scroll;
  align-items: center;
}

.preview::-webkit-scrollbar {
  display: none;
}

.img-box {
  display: flex;
  position: relative;

  .img {
    width: 100px;
    height: 100px;
    object-fit: contain;
    border-radius: 2px;
  }

  .img-preview {
    position: absolute;
    top: 0;
    left: 0;
    font-size: 8px;
    padding: 2px;
    color: var(--color-bg-2);
    border-radius: 2px 0px 2px 0px;
    background-color: rgb(var(--primary-6));
  }

  .selected {
    position: absolute;
    top: 50%;
    right: 50%;
    transform: translate(50%, -50%);
    font-size: 24px;
    font-weight: bold;
    color: rgb(var(--primary-6));
    user-select: none;
  }

  .img-index {
    position: absolute;
    top: 2px;
    right: 2px;
    color: var(--color-bg-2);
    font-size: 12px;
    text-align: center;
    overflow: hidden;
    color: rgb(var(--primary-6));
  }

  .img-name {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-radius: 0px 0px 2px 2px;
    color: var(--color-bg-2);
    font-size: 8px;
    text-align: center;
    overflow: hidden;
    background-color: rgb(var(--primary-6));
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.col {
  flex: 1;
  display: flex;
  flex-direction: row;

  .col-1 {
    width: 65%;
    display: flex;
    position: relative;
    overflow: hidden;
    border-radius: 4px;

    .img-preview-box {
      display: flex;
      position: relative;
      width: 100%;
      height: 100%;

      .img {
        position: absolute;
        width: 100%;
        height: 100%;
        object-fit: contain;
      }
    }

    .tools {
      right: 8px;
      top: 8px;
      position: absolute;
    }
  }

  .col-2 {
    flex: 1;
    display: flex;
    position: relative;
    flex-direction: column;
    justify-content: space-between;

    #canvas {
      position: absolute;
      bottom: 0;
      width: 100%;
    }
  }

  .icon-btn {
    border: none;
    transition: 0.3s ease;
    color: var(--color-neutral-8);
  }

  .icon-btn:hover {
    color: rgb(var(--primary-6));
  }

  .icon-btn:active {
    transform: scale(0.9);
  }

  .op {
    display: flex;
    flex-direction: row;
    align-items: center;
  }

  .slider {
    width: 70%;
    padding-left: 16px;
    padding-right: 16px;
    --slider-connect-bg: rgb(var(--primary-6));
    --slider-tooltip-bg: rgb(var(--primary-6));
    --slider-handle-ring-color: rgb(var(--primary-6));
  }

  .icon-box {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 8px;
  }
}
</style>
