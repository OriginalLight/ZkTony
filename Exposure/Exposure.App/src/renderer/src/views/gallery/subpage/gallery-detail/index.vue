<template>
  <div class="box">
    <a-space direction="vertical" class="preview">
      <a-tag
        v-if="subpage.list.light.length > 0"
        style="width: 100px; display: flex; justify-content: center"
        color="arcoblue"
        >{{ t('gallery.detail.picture.light') }}</a-tag
      >
      <div
        v-for="img in subpage.list.light"
        :key="img.id"
        class="img-box"
        @click="handelSelected(img)"
      >
        <img v-lazy="img.thumbnail" class="img" />
        <icon-check v-if="showSelected(img)" class="selected" />
        <div class="img-name">{{ img.name }}</div>
      </div>
      <a-tag
        v-if="subpage.list.dark.length > 0"
        style="width: 100px; display: flex; justify-content: center"
        color="arcoblue"
        >{{ t('gallery.detail.picture.dark') }}</a-tag
      >
      <div
        v-for="img in subpage.list.dark"
        :key="img.id"
        class="img-box"
        @click="handelSelected(img)"
      >
        <img v-lazy="img.thumbnail" class="img" />
        <icon-check v-if="showSelected(img)" class="selected" />
        <div class="img-name">{{ img.name }}</div>
      </div>
      <a-tag
        v-if="subpage.list.combine.length > 0"
        style="width: 100px; display: flex; justify-content: center"
        color="arcoblue"
        >{{ t('gallery.detail.picture.combine') }}</a-tag
      >
      <div
        v-for="img in subpage.list.combine"
        :key="img.id"
        class="img-box"
        @click="handelSelected(img)"
      >
        <img v-lazy="img.thumbnail" class="img" />
        <icon-check v-if="showSelected(img)" class="selected" />
        <div class="img-name">{{ img.name }}</div>
      </div>
    </a-space>
    <a-divider direction="vertical" style="height: 100%" :margin="8" />
    <div class="col">
      <div class="col-1">
        <div class="img-preview-box">
          <img
            id="img-preview"
            v-lazy="subpage.item?.path"
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
            <a-button type="primary" size="medium" @click="resetOptions">
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
              :loading="loading"
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
          <a-input v-model="subpage.item.name" allow-clear @change="handleUpdate()">
            <template #prepend> {{ t('gallery.detail.name') }} </template>
          </a-input>
          <a-input :model-value="subpage.item.width + ' x ' + subpage.item.height" readonly>
            <template #prepend> {{ t('gallery.detail.size') }} </template>
          </a-input>
          <a-input :model-value="(subpage.item.exposureTime / 1000).toString()" readonly>
            <template #prepend> {{ t('gallery.detail.expo') }} </template>
            <template #append> {{ t('gallery.detail.unit') }} </template>
          </a-input>
          <a-input :model-value="user?.name" readonly>
            <template #prepend> {{ t('gallery.detail.user') }} </template>
          </a-input>
          <a-input :model-value="subpage.item.createTime" readonly>
            <template #prepend> {{ t('gallery.detail.time') }} </template>
          </a-input>
          <a-divider margin="0" />
          <a-radio-group v-model="radioOpts.opt1" type="button" style="width: 100%">
            <a-radio :value="0" style="width: 100%; text-align: center">
              <a-space>
                <icon-eye />
                <div>{{ t('gallery.detail.preview.adjust') }}</div>
              </a-space>
            </a-radio>
            <a-radio :value="1" style="width: 100%; text-align: center">
              <a-space>
                <icon-edit />
                <div>{{ t('gallery.detail.picture.adjust') }}</div>
              </a-space>
            </a-radio>
          </a-radio-group>

          <div v-if="radioOpts.opt1 === 0">
            <a-row style="text-align: center; padding-top: 16px">
              <a-col :span="8">
                <a-tooltip :content="t('gallery.detail.rotate.left')">
                  <div @click="options.rotate -= 90">
                    <icon-rotate-left class="icon-btn" :size="32" />
                  </div>
                </a-tooltip>
              </a-col>
              <a-col :span="8">
                <a-tooltip :content="t('gallery.detail.rotate.right')">
                  <div @click="options.rotate += 90">
                    <icon-rotate-right class="icon-btn" :size="32" />
                  </div>
                </a-tooltip>
              </a-col>
            </a-row>
          </div>
          <div v-if="radioOpts.opt1 === 1">
            <a-space direction="vertical" style="width: 100%">
              <a-radio-group v-model="radioOpts.opt2" type="button" style="width: 100%">
                <a-radio :value="0" style="width: 100%; text-align: center">
                  <a-space>
                    <Brightness />
                    <div>{{ t('gallery.detail.brightness') }}</div>
                  </a-space>
                </a-radio>
                <a-radio :value="1" style="width: 100%; text-align: center">
                  <a-space>
                    <Contrast />
                    <div>{{ t('gallery.detail.contrast') }}</div>
                  </a-space>
                </a-radio>
                <a-radio :value="2" style="width: 100%; text-align: center">
                  <a-space>
                    <Transform />
                    <div>{{ t('gallery.detail.invert') }}</div>
                  </a-space>
                </a-radio>
              </a-radio-group>

              <div v-if="radioOpts.opt2 === 0" class="slider-box">
                <Slider v-model="options.brightness" class="slider" :max="200" :lazy="false" />
              </div>

              <div v-if="radioOpts.opt2 === 1" class="slider-box">
                <Slider v-model="options.contrast" class="slider" :max="200" :lazy="false" />
              </div>

              <div v-if="radioOpts.opt2 === 2" class="icon-box">
                <a-switch v-model="options.invert" />
              </div>
            </a-space>
          </div>
        </a-space>
        <canvas id="canvas"></canvas>
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import {
  CoordinateSystem,
  Refresh,
  Save,
  Brightness,
  Contrast,
  Transform
} from '@icon-park/vue-next'
import { Message } from '@arco-design/web-vue'
import useGalleryState from '@renderer/states/gallery'
import { User, getUserById } from '@renderer/api/user'
import { Picture, updatePicture, adjustPicture } from '@renderer/api/picture'
import Slider from '@vueform/slider'

const { subpage } = useGalleryState()

const { t } = useI18n()
const router = useRouter()

const user = ref<User>()

const options = ref({
  brightness: 100,
  contrast: 100,
  grayScale: 0,
  rotate: 0,
  invert: false,
  scale: 1
})

const radioOpts = ref({
  opt1: 0,
  opt2: 0
})

const loading = ref(false)

const resetOptions = () => {
  options.value = {
    brightness: 100,
    contrast: 100,
    invert: false,
    grayScale: 0,
    rotate: 0,
    scale: 1
  }
  const img = document.getElementById('img-preview') as HTMLImageElement
  img.style.left = '0px'
  img.style.top = '0px'
}

const showSelected = (img: Picture) => {
  return subpage.value.item.id === img.id
}

const handle3dChart = async () => {
  router.push('/gallery-chart')
}

const handelSelected = async (img: Picture) => {
  subpage.value.item = img
  handleHistogram()
  if (img.userId === user.value?.id) {
    return
  }
  try {
    const res = await getUserById(img.userId)
    user.value = res.data
  } catch (error) {
    console.error(error)
  }
}

const handleUpdate = async () => {
  try {
    await updatePicture({
      id: subpage.value.item.id,
      name: subpage.value.item.name
    })
  } catch (error) {
    console.error(error)
  }
}

const handleSave = async () => {
  try {
    loading.value = true
    const res = await adjustPicture({
      id: subpage.value.item.id,
      brightness: options.value.brightness,
      contrast: options.value.contrast,
      invert: options.value.invert
    })
    subpage.value.item = res.data
    resetOptions()
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

const handleHistogram = () => {
  const canvas = document.getElementById('canvas') as HTMLCanvasElement
  const ctx = canvas.getContext('2d') as CanvasRenderingContext2D
  const img = new Image()

  img.onload = () => {
    canvas.width = img.width
    canvas.height = img.height
    ctx.drawImage(img, 0, 0, img.width, img.height)
    const originalImageData = ctx.getImageData(0, 0, img.width, img.height)
    const grayList = new Array(256)

    const imageData = ctx.createImageData(originalImageData)

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
      imageData.data[i + 0] = gray // Red
      imageData.data[i + 1] = gray // Green
      imageData.data[i + 2] = gray // Blue
      imageData.data[i + 3] = originalImageData.data[i + 3] // Alpha

      grayList[gray] += 1 // 统计灰度值数量
    }

    ctx.putImageData(imageData, 0, 0)

    canvas.width = 256 * 2
    canvas.height = (Math.max(...grayList) + 100 - (Math.max(...grayList) % 100)) / 20
    ctx.strokeStyle = '#000000'
    ctx.lineWidth = 1
    for (let i = 0; i < grayList.length; i++) {
      ctx.beginPath()
      ctx.moveTo(i * 2, canvas.height)
      ctx.lineTo(i * 2, grayList[i] / 20)
      ctx.stroke()
    }
  }

  img.src = subpage.value.item.thumbnail
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
    }
  })
  img.addEventListener('touchend', () => {
    isDown = false
  })
}

onMounted(() => {
  handelSelected(subpage.value.item)
  handleHistogram()
  handleTouch()
})
</script>

<style scoped>
.box {
  display: flex;
  flex-direction: row;
  padding: 16px;
  height: calc(100vh - 66px - 32px);
}

.preview {
  overflow-x: hidden;
  overflow-y: scroll;
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
  }

  .selected {
    position: absolute;
    top: 0;
    right: 0;
    color: rgb(var(--arcoblue-6));
    font-size: 20px;
    padding: 2px;
  }

  .img-name {
    position: absolute;
    bottom: 0;
    width: 100%;
    background-color: rgba(0, 0, 0, 1);
    color: var(--color-bg-2);
    font-size: 10px;
    text-align: center;
    overflow: hidden;
  }
}

.col {
  flex: 1;
  display: flex;
  flex-direction: row;

  .col-1 {
    width: 70%;
    display: flex;
    position: relative;
    overflow: hidden;

    .img-preview-box {
      display: flex;
      position: relative;
      width: 100%;
      height: 100%;

      .img {
        position: absolute;
        width: 100%;
        height: 100%;
        object-fit: fill;
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
    flex-direction: column;
    justify-content: space-between;

    #canvas {
      width: 100%;
      height: 35%;
    }
  }

  .icon-btn {
    border: none;
    transition: 0.3s ease;
    color: var(--color-neutral-8);
  }

  .icon-btn:hover {
    color: rgb(var(--arcoblue-6));
  }

  .icon-btn:active {
    transform: scale(0.9);
  }

  .slider-box {
    padding: 8px;
    padding-top: 36px;

    .slider {
      --slider-connect-bg: rgb(var(--arcoblue-6));
      --slider-tooltip-bg: rgb(var(--arcoblue-6));
      --slider-handle-ring-color: rgb(var(--arcoblue-6));
    }
  }

  .icon-box {
    display: flex;
    justify-content: center;
    align-items: center;
    padding: 8px;
  }
}
</style>
