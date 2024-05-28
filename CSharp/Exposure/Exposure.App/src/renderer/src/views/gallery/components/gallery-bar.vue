<template>
  <div class="bar">
    <a-space size="large">
      <a-range-picker v-model="options.date" style="width: 280px" />
      <a-input
        v-model="options.name"
        :placeholder="t('gallery.bar.name.placeholder')"
        style="width: 180px"
        allow-clear
      />
      <a-button type="primary" shape="round" @click="handleSearch">
        <template #icon>
          <icon-search />
        </template>
        <template #default>{{ t('gallery.bar.search') }}</template></a-button
      >
    </a-space>
    <a-space size="large">
      <a-button type="primary" shape="round" :disabled="diableDetail" @click="handleDetail">
        <template #icon>
          <icon-edit />
        </template>
        <template #default>{{ t('gallery.bar.detail') }}</template>
      </a-button>
      <a-button
        type="primary"
        shape="round"
        :disabled="diableCombine"
        :loading="loading.combine"
        @click="handleCombine"
      >
        <template #icon>
          <icon-common />
        </template>
        <template #default>{{ t('gallery.bar.combine') }}</template>
      </a-button>
      <a-popover position="br" trigger="click">
        <a-button type="primary" shape="round" :disabled="disableExport" :loading="loading.export">
          <template #icon>
            <icon-export />
          </template>
          <template #default>{{ t('gallery.bar.export') }}</template>
        </a-button>
        <template #content>
          <a-space size="large">
            <a-button shape="round" @click="handleExport('png')">PNG</a-button>
            <a-button shape="round" @click="handleExport('tiff')">TIFF</a-button>
            <a-button shape="round" @click="handleExport('jpg')">JPG</a-button>
          </a-space>
        </template>
      </a-popover>
      <a-button
        type="primary"
        shape="round"
        status="danger"
        :loading="loading.delete"
        :disabled="disableDelete"
        @click="visible = true"
      >
        <template #icon>
          <icon-delete />
        </template>
        <template #default>{{ t('gallery.bar.delete') }}</template>
      </a-button>
    </a-space>
  </div>
  <a-modal v-model:visible="visible" draggable @ok="handleDelete" @cancel="visible = false">
    <template #title> {{ t('gallery.bar.delete.title') }} </template>
    <div>
      {{ t('gallery.bar.delete.content') }}
    </div>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { combinePicture, exportPicture, deletePicture } from '@renderer/api/picture'
import { Message } from '@arco-design/web-vue'
import useGalleryState from '@renderer/states/gallery'

const { selected, options } = useGalleryState()

const emit = defineEmits(['search', 'combine'])

const { t } = useI18n()
const router = useRouter()

const visible = ref(false)

const loading = ref({
  combine: false,
  export: false,
  delete: false
})

// 是否禁用详细
const diableDetail = computed(() => {
  // 数量不等于1
  return selected.value.length !== 1
})

// 是否禁用合并
const diableCombine = computed(() => {
  // 数量等于2, 类型一个是白光一个是曝光, 不能同时是白光或者曝光
  return (
    selected.value.length !== 2 ||
    selected.value[0].type === selected.value[1].type ||
    selected.value.some((item) => item.type === 2) ||
    loading.value.combine
  )
})

// 是否禁用导出
const disableExport = computed(() => {
  // 数量等于0
  return selected.value.length === 0 || loading.value.export
})

// 是否禁用删除
const disableDelete = computed(() => {
  // 数量等于0
  return selected.value.length === 0 || loading.value.delete
})

// 详细
const handleDetail = () => {
  router.push('/gallery-detail')
}

// 搜索
const handleSearch = () => {
  emit('search')
}

// 合并
const handleCombine = async () => {
  try {
    loading.value.combine = true
    const ids: number[] = selected.value.map((item) => item.id)
    await combinePicture(ids)
    emit('combine')
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
    const ids: number[] = selected.value.map((item) => item.id)
    await exportPicture({ ids: ids, format: format })
    Message.success(t('gallery.bar.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.export = false
  }
}

// 删除
const handleDelete = async () => {
  visible.value = false
  try {
    loading.value.delete = true
    const ids: number[] = selected.value.map((item) => item.id)
    await deletePicture(ids)
    selected.value = []
    emit('search')
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.delete = false
  }
}
</script>

<style scoped lang="less">
.bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
}
</style>
