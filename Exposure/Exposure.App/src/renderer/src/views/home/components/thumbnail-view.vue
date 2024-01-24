<template>
  <a-card class="card" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">
    <div style="display: flex; justify-content: end">
      <a-space size="large">
        <a-button
          type="primary"
          style="width: 100px"
          size="large"
          :disabled="diableCombine"
          :loading="loading.combine"
          @click="handleCombine"
        >
          <template #icon>
            <icon-common />
          </template>
          {{ t('home.thumbnail.view.combine') }}</a-button
        >
        <a-popover position="br" trigger="click">
          <a-button
            type="primary"
            style="width: 100px"
            size="large"
            :disabled="disableExport"
            :loading="loading.export"
          >
            <template #icon>
              <icon-export />
            </template>
            {{ t('home.thumbnail.view.export') }}</a-button
          >
          <template #content>
            <a-space size="large">
              <a-button @click="handleExport('png')">PNG</a-button>
              <a-button @click="handleExport('tiff')">TIFF</a-button>
              <a-button @click="handleExport('jpg')">JPG</a-button>
            </a-space>
          </template>
        </a-popover>
      </a-space>
    </div>
    <div class="grid-item">
      <div style="font-size: small">{{ t('home.thumbnail.view.picture.light') }}</div>
      <a-space v-dragscroll :size="5" class="scroll-wrapper">
        <div v-for="item in light" :key="item.id" class="scroll-item" @click="handleSelected(item)">
          <div v-if="showPreview(item)" class="img-preview">Preview</div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img class="img" alt="dessert" :src="item.path" />
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
    <div class="grid-item">
      <div style="font-size: small">{{ t('home.thumbnail.view.picture.dark') }}</div>
      <a-space v-dragscroll :size="5" class="scroll-wrapper">
        <div v-for="item in dark" :key="item.id" class="scroll-item" @click="handleSelected(item)">
          <div v-if="showPreview(item)" class="img-preview">Preview</div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img class="img" alt="dessert" :src="item.path" />
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
    <div class="grid-item">
      <div style="font-size: small">{{ t('home.thumbnail.view.picture.combine') }}</div>
      <a-space v-dragscroll :size="5" class="scroll-wrapper">
        <div
          v-for="item in combine"
          :key="item.id"
          class="scroll-item"
          @click="handleSelected(item)"
        >
          <div v-if="showPreview(item)" class="img-preview">Preview</div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img class="img" alt="dessert" :src="item.path" />
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import { Picture, combinePicture, exportPicture } from '@renderer/api/picture'
import dayjs from 'dayjs'

const props = defineProps({
  preview: {
    type: Object as () => Picture,
    required: true
  },
  thumbnail: {
    type: Object as () => Picture[],
    required: true
  },
  thumbnailSelected: {
    type: Object as () => Picture[],
    required: true
  }
})

const emit = defineEmits(['selected', 'combine'])

const { t } = useI18n()

const loading = ref({
  combine: false,
  export: false
})

const diableCombine = computed(() => {
  // 数量等于2, 类型一个是白光一个是曝光, 不能同时是白光或者曝光
  return (
    props.thumbnailSelected.length !== 2 ||
    props.thumbnailSelected[0].type === props.thumbnailSelected[1].type ||
    props.thumbnailSelected.some((item) => item.type === 3) ||
    loading.value.combine
  )
})

const disableExport = computed(() => {
  // 数量等于0
  return props.thumbnailSelected.length === 0 || loading.value.export
})

// 白光
const light = computed(() => {
  return props.thumbnail
    .filter((item) => item.type === 0)
    .sort((a, b) => {
      return dayjs(b.createTime).valueOf() - dayjs(a.createTime).valueOf()
    })
})

// 曝光
const dark = computed(() => {
  return props.thumbnail
    .filter((item) => item.type === 1)
    .sort((a, b) => {
      return dayjs(b.createTime).valueOf() - dayjs(a.createTime).valueOf()
    })
})

// 合成
const combine = computed(() => {
  return props.thumbnail
    .filter((item) => item.type === 2)
    .sort((a, b) => {
      return dayjs(b.createTime).valueOf() - dayjs(a.createTime).valueOf()
    })
})

// 是否显示选中
const showSelected = (item: Picture) => {
  return props.thumbnailSelected.some((selected) => selected.id === item.id)
}

// 是否显示预览
const showPreview = (item: Picture) => {
  return props.preview.id === item.id
}

// 选择
const handleSelected = (item: Picture) => {
  emit('selected', item)
}

// 合成
const handleCombine = async () => {
  try {
    loading.value.combine = true
    const ids: number[] = props.thumbnailSelected.map((item) => item.id)
    const res = await combinePicture(ids)
    emit('combine', res.data)
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.combine = false
  }
}

// 导出
const handleExport = async (format: string) => {
  try {
    loading.value.export = true
    const ids: number[] = props.thumbnailSelected.map((item) => item.id)
    await exportPicture({ ids: ids, format: format })
    Message.success(t('home.thumbnail.view.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.export = false
  }
}
</script>

<style scoped lang="less">
.card {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin-top: 4px;
}

.grid-item {
  display: flex;
  flex-direction: column;
  flex: 1;
  height: 100%;
  min-height: 100px;
}

.scroll-wrapper {
  display: flex;
  height: 100%;
  overflow: hidden;

  .scroll-item {
    position: relative;
    .img-preview {
      position: absolute;
      top: 0;
      left: 0;
      color: rgb(var(--arcoblue-6));
      font-size: 12px;
      padding: 2px;
    }

    .selected {
      position: absolute;
      top: 0;
      right: 0;
      color: rgb(var(--arcoblue-6));
      font-size: 20px;
      padding: 2px;
    }

    .img {
      border: 1px solid var(--color-neutral-6);
      height: 100px;
      min-height: 100px;
      width: auto;
      object-fit: contain;
    }

    .img-name {
      position: absolute;
      bottom: 4px;
      width: 100%;
      background-color: rgba(0, 0, 0, 0.5);
      color: var(--color-neutral-0);
      font-size: 10px;
      text-align: center;
      overflow: hidden;
    }
  }
}

.scroll-wrapper::-webkit-scrollbar {
  display: none;
}
</style>
