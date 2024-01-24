<template>
  <a-image
    :src="props.image.path"
    fit="fill"
    width="100%"
    height="100%"
    :style="{
      filter: `brightness(${imageOptions.brightness / 30.0}) contrast(${imageOptions.contrast}%)`
    }"
  />
  <div class="image-edit">
    <a-space size="medium">
      <a-tooltip placement="top" :content="t('home.image.preview.brightness')">
        <a-popover position="bottom" trigger="click">
          <a-button shape="round" type="outline">
            <template #icon>
              <Brightness />
            </template>
          </a-button>
          <template #content>
            <a-slider v-model="imageOptions.brightness" :style="{ width: '300px' }" show-input />
          </template>
        </a-popover>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.contrast')">
        <a-popover position="bottom" trigger="click">
          <a-button shape="round" type="outline">
            <template #icon>
              <Contrast />
            </template>
          </a-button>
          <template #content>
            <a-slider
              v-model="imageOptions.contrast"
              :style="{ width: '300px' }"
              show-input
              :max="300"
            />
          </template>
        </a-popover>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.undo')">
        <a-button shape="round" type="outline" @click="undo">
          <template #icon>
            <Undo />
          </template>
        </a-button>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.redo')">
        <a-button
          shape="round"
          type="outline"
          @click="(imageOptions.brightness = 30), (imageOptions.contrast = 100)"
        >
          <template #icon>
            <Refresh />
          </template>
        </a-button>
      </a-tooltip>
      <a-tooltip placement="top" :content="t('home.image.preview.save')">
        <a-button
          shape="round"
          type="outline"
          :disabled="
            (imageOptions.brightness === 30 && imageOptions.contrast === 100) ||
            props.image.name === 'Preview'
          "
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
      <a-tag color="arcoblue">{{ props.image.name }}</a-tag>
      <a-tag color="arcoblue">{{ props.image.width + ' x ' + props.image.height }}</a-tag>
    </a-space>
  </div>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Brightness, Contrast, Undo, Refresh, Save } from '@icon-park/vue-next'
import { useRefHistory } from '@vueuse/core'
import { Picture, adjustPicture } from '@renderer/api/picture'
import { Message } from '@arco-design/web-vue'

const props = defineProps({
  image: {
    type: Object as () => Picture,
    required: true
  }
})

const emit = defineEmits(['adjust'])

const { t } = useI18n()

// 参数
const imageOptions = ref({
  brightness: 30,
  contrast: 100
})

// 撤销
const { undo } = useRefHistory(imageOptions, {
  deep: true
})

// 保存
const handleSave = async () => {
  try {
    const res = await adjustPicture({
      id: props.image.id,
      brightness: imageOptions.value.brightness,
      contrast: imageOptions.value.contrast
    })
    emit('adjust', res.data)
  } catch (error) {
    Message.error((error as Error).message)
  }
}
</script>

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
</style>
