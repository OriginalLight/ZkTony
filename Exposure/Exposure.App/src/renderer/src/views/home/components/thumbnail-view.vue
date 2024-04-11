<template>
  <a-card class="card" :body-style="{ height: '100%', display: 'flex', flexDirection: 'column' }">
    <div style="display: flex; justify-content: end">
      <a-space size="large">
        <a-button
          type="primary"
          style="width: 100px"
          size="large"
          shape="round"
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
            shape="round"
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
              <a-button shape="round" @click="handleExport('png')">PNG</a-button>
              <a-button shape="round" @click="handleExport('tiff')">TIFF</a-button>
              <a-button shape="round" @click="handleExport('jpg')">JPG</a-button>
            </a-space>
          </template>
        </a-popover>
      </a-space>
    </div>
    <div class="grid-item">
      <a-space>
        <a-tag style="width: 100px" size="small">
          <div style="width: 100%; text-align: center">
            {{ t('home.thumbnail.view.picture.light') }}
          </div>
        </a-tag>
        <a-tag style="width: 40px" size="small">
          <div style="width: 100%; text-align: center">{{ light.length }}</div>
        </a-tag>
      </a-space>
      <a-space id="scroll-light" :size="5" class="scroll-wrapper">
        <div
          v-for="(item, index) in light"
          :key="item.id"
          class="scroll-item"
          @click="handleSelected(item)"
        >
          <div v-if="showPreview(item)" class="img-preview">
            {{ t('home.thumbnail.view.preview') }}
          </div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img v-lazy="item.thumbnail" class="img" alt="dessert" />
          <div class="img-index">{{ index + 1 }}</div>
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
    <div class="grid-item">
      <a-space>
        <a-tag style="width: 100px" size="small">
          <div style="width: 100%; text-align: center">
            {{ t('home.thumbnail.view.picture.dark') }}
          </div>
        </a-tag>
        <a-tag style="width: 40px" size="small">
          <div style="width: 100%; text-align: center">{{ dark.length }}</div>
        </a-tag>
      </a-space>
      <a-space id="scroll-dark" :size="5" class="scroll-wrapper">
        <div
          v-for="(item, index) in dark"
          :key="item.id"
          class="scroll-item"
          @click="handleSelected(item)"
        >
          <div v-if="showPreview(item)" class="img-preview">
            {{ t('home.thumbnail.view.preview') }}
          </div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img v-lazy="item.thumbnail" class="img" alt="dessert" />
          <div class="img-index">{{ index + 1 }}</div>
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
    <div class="grid-item">
      <a-space>
        <a-tag style="width: 100px" size="small">
          <div style="width: 100%; text-align: center">
            {{ t('home.thumbnail.view.picture.combine') }}
          </div>
        </a-tag>
        <a-tag style="width: 40px" size="small">
          <div style="width: 100%; text-align: center">{{ combine.length }}</div>
        </a-tag>
      </a-space>
      <a-space id="scroll-combine" :size="5" class="scroll-wrapper">
        <div
          v-for="(item, index) in combine"
          :key="item.id"
          class="scroll-item"
          @click="handleSelected(item)"
        >
          <div v-if="showPreview(item)" class="img-preview">
            {{ t('home.thumbnail.view.preview') }}
          </div>
          <icon-check v-if="showSelected(item)" class="selected" />
          <img v-lazy="item.thumbnail" class="img" alt="dessert" />
          <div class="img-index">{{ index + 1 }}</div>
          <div class="img-name">{{ item.name }}</div>
        </div>
      </a-space>
    </div>
  </a-card>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted } from 'vue'
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
  selected: {
    type: Object as () => Picture[],
    required: true
  }
})
const emit = defineEmits(['selected', 'combine'])
const { t } = useI18n()

// 加载
const loading = ref({
  combine: false,
  export: false
})
// 是否禁用合并
const diableCombine = computed(() => {
  // 数量等于2, 类型一个是白光一个是曝光, 不能同时是白光或者曝光
  return (
    props.selected.length !== 2 ||
    props.selected[0].type === props.selected[1].type ||
    props.selected.some((item) => item.type === 2) ||
    loading.value.combine
  )
})

// 是否禁用导出
const disableExport = computed(() => {
  // 数量等于0
  return props.selected.length === 0 || loading.value.export
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
  return props.selected.some((selected) => selected.id === item.id)
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
    const ids: number[] = props.selected.map((item) => item.id)
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
    const ids: number[] = props.selected.map((item) => item.id)
    await exportPicture({ ids: ids, format: format })
    Message.success(t('home.thumbnail.view.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.export = false
  }
}

// 给div添加鼠标滚动
const mouseWhell = (id: string) => {
  const div = document.getElementById(id)
  if (div) {
    div.addEventListener('wheel', (e) => {
      e.preventDefault()
      div.scrollLeft += e.deltaY / 2
    })
  }
}

onMounted(() => {
  mouseWhell('scroll-light')
  mouseWhell('scroll-dark')
  mouseWhell('scroll-combine')
})
</script>

<style scoped lang="less">
.card {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin-top: 8px;
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
  overflow-y: hidden;
  overflow-x: scroll;

  .scroll-item {
    display: flex;
    position: relative;
    .img-preview {
      position: absolute;
      top: 0;
      left: 0;
      font-size: 10px;
      padding: 2px;
      color: var(--color-bg-2);
      border-radius: 2px;
      background-color: rgb(var(--primary-6));
    }

    .selected {
      position: absolute;
      top: 0;
      right: 0;
      color: var(--color-bg-2);
      border-radius: 2px;
      background-color: rgb(var(--primary-6));
    }

    .img {
      height: 90px;
      min-height: 90px;
      width: auto;
      object-fit: contain;
      border-radius: 4px;
    }

    .img-index {
      position: absolute;
      bottom: 10px;
      width: 25%;
      color: var(--color-bg-2);
      font-size: 8px;
      text-align: center;
      overflow: hidden;
      border-radius: 2px;
      background-color: rgb(var(--primary-6));
    }

    .img-name {
      position: absolute;
      bottom: 0;
      width: 100%;
      color: var(--color-bg-2);
      font-size: 10px;
      text-align: center;
      overflow: hidden;
      border-radius: 4px;
      background-color: rgb(var(--primary-6));
    }
  }
}

.scroll-wrapper::-webkit-scrollbar {
  display: none;
}
</style>
