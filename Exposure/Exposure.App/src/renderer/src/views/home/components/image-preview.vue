<template>
  <div class="img-preview-box">
    <img
      id="img-preview"
      v-lazy="props.image.path"
      :style="{
        filter: `brightness(${imageOptions.brightness}%) contrast(${imageOptions.contrast}%)`,
        transform: `scale(${imageOptions.scale})`
      }"
    />
  </div>
  <div class="image-edit">
    <a-space size="medium">
      <a-tooltip placement="top" :content="t('home.image.preview.brightness')">
        <a-popover position="br" trigger="click">
          <a-button type="primary" size="medium">
            <template #icon>
              <Brightness />
            </template>
          </a-button>
          <template #content>
            <a-space size="medium">
              <Slider
                v-model="imageOptions.brightness"
                class="slider"
                :style="{ width: '300px' }"
                :max="300"
                show-tooltip="drag"
                :lazy="false"
              />
              <div style="width: 30px; text-align: center">{{ imageOptions.brightness }}</div>
            </a-space>
          </template>
        </a-popover>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.contrast')">
        <a-popover position="br" trigger="click">
          <a-button type="primary" size="medium">
            <template #icon>
              <Contrast />
            </template>
          </a-button>
          <template #content>
            <a-space size="medium">
              <Slider
                v-model="imageOptions.contrast"
                class="slider"
                :style="{ width: '300px' }"
                :max="300"
                show-tooltip="drag"
                :lazy="false"
              />
              <div style="width: 30px; text-align: center">{{ imageOptions.contrast }}</div>
            </a-space>
          </template>
        </a-popover>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.redo')">
        <a-button
          type="primary"
          size="medium"
          :disabled="
            imageOptions.brightness == 100 &&
            imageOptions.contrast == 100 &&
            imageOptions.scale == 1.2
          "
          @click="handleRedo"
        >
          <template #icon>
            <Refresh />
          </template>
        </a-button>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.save')">
        <a-button
          type="primary"
          size="medium"
          :disabled="
            (imageOptions.brightness === 100 && imageOptions.contrast === 100) ||
            props.image.name === 'Preview'
          "
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
  <div class="image-info">
    <a-space size="medium">
      <a-tag>{{ props.image.name }}</a-tag>
      <a-tag>{{ props.image.width + ' x ' + props.image.height }}</a-tag>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import { ref, watch, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Brightness, Contrast, Refresh, Save } from '@icon-park/vue-next'
import { Picture, adjustPicture } from '@renderer/api/picture'
import { Message } from '@arco-design/web-vue'
import Slider from '@vueform/slider'

const props = defineProps({
  image: {
    type: Object as () => Picture,
    required: true
  }
})

const emit = defineEmits(['adjust'])

const { t } = useI18n()

// 加载状态
const loading = ref(false)

// 参数
const imageOptions = ref({
  brightness: 100,
  contrast: 100,
  scale: 1.2
})

// 恢复
const handleRedo = () => {
  imageOptions.value.brightness = 100
  imageOptions.value.contrast = 100
  imageOptions.value.scale = 1.2
  const img = document.getElementById('img-preview') as HTMLImageElement
  img.style.left = '0px'
  img.style.top = '0px'
}

// 保存
const handleSave = async () => {
  try {
    loading.value = true
    const res = await adjustPicture({
      id: props.image.id,
      brightness: imageOptions.value.brightness,
      contrast: imageOptions.value.contrast,
      invert: false,
      code: 0
    })
    emit('adjust', res.data)
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.image,
  () => {
    imageOptions.value.brightness = 100
    imageOptions.value.contrast = 100
  }
)

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
      imageOptions.value.scale = scale
    }
  })

  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  div.addEventListener('touchend', (_e) => {
    startX = startY = distanceStart = distanceEnd = 0
  })

  // 鼠标滚轮缩放
  div.addEventListener('wheel', (e) => {
    if (e.deltaY < 0) {
      imageOptions.value.scale += 0.1
    } else {
      imageOptions.value.scale -= 0.1
    }
  })
}

onMounted(() => {
  handleTouch()
})
</script>

<style src="@vueform/slider/themes/default.css"></style>

<style scoped lang="less">
.image-edit {
  position: absolute;
  top: 8px;
  right: 8px;
  padding: 12px;
}

.image-info {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 12px;
}

.img-preview-box {
  display: flex;
  position: relative;
  height: 100%;
  border: 1px solid var(--color-neutral-3);
  border-radius: var(--border-radius-small);
  background-color: var(--color-bg-2);

  img {
    position: absolute;
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

img[src=''],
img:not([src]) {
  opacity: 0;
}

.slider {
  --slider-connect-bg: rgb(var(--primary-6));
  --slider-tooltip-bg: rgb(var(--primary-6));
  --slider-handle-ring-color: rgb(var(--primary-6));
}
</style>
