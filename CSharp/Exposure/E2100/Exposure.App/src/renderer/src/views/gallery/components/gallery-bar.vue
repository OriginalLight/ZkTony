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
      <a-button
        type="primary"
        shape="round"
        :loading="loading.update"
        :disabled="selected.length !== 1 || loading.update"
        @click="showUpdate"
      >
        <template #icon>
          <icon-edit />
        </template>
        <template #default>{{ t('gallery.bar.rename') }}</template>
      </a-button>
      <a-button
        type="primary"
        shape="round"
        :disabled="selected.length !== 1"
        @click="handleDetail"
      >
        <template #icon>
          <icon-image />
        </template>
        <template #default>{{ t('gallery.bar.detail') }}</template>
      </a-button>
      <a-popover position="br" trigger="click">
        <a-button
          type="primary"
          shape="round"
          :disabled="selected.length === 0 || loading.export"
          :loading="loading.export"
        >
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
        :disabled="selected.length === 0 || loading.delete"
        @click="visible.delete = true"
      >
        <template #icon>
          <icon-delete />
        </template>
        <template #default>{{ t('gallery.bar.delete') }}</template>
      </a-button>
    </a-space>
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
  <a-modal
    v-model:visible="visible.update"
    draggable
    @ok="handleUpdate"
    @cancel="visible.update = false"
  >
    <template #title> {{ t('gallery.bar.rename') }} </template>
    <a-input v-model="update.name" allow-clear :max-length="32"> </a-input>
  </a-modal>
</template>

<script lang="ts" setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { Message } from '@arco-design/web-vue'
import useGalleryState from '@renderer/states/gallery'
import useHomeState from '@renderer/states/home'
import { deleteAlbum, exportAlbum, updateAlbum } from '@renderer/api/album'

const { selected, options, subpage } = useGalleryState()

const { selectedAlbums, albums, albumPreview, preview } = useHomeState()

const emit = defineEmits(['search', 'update'])

const { t } = useI18n()
const router = useRouter()

const visible = ref({
  delete: false,
  update: false
})

const loading = ref({
  update: false,
  export: false,
  delete: false
})

const update = ref({
  id: 0,
  name: ''
})

// 详细
const handleDetail = () => {
  if (selected.value.length === 0) {
    return
  }
  const item = selected.value[0]
  if (!item || item.photos.length === 0) {
    return
  }
  subpage.value = {
    album: item,
    selected: [],
    preview: item.photos.find((photo) => photo.type === 1) ?? item.photos[0]
  }
  router.push('/gallery-detail')
}

// 搜索
const handleSearch = () => {
  emit('search')
}

// 导出
const handleExport = async (format: string) => {
  try {
    loading.value.export = true
    const ids: number[] = selected.value.map((item) => item.id)
    await exportAlbum({ ids: ids, format: format })
    Message.success(t('gallery.bar.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.export = false
  }
}

// 删除
const handleDelete = async () => {
  visible.value.delete = false
  try {
    loading.value.delete = true
    const ids: number[] = selected.value.map((item) => item.id)
    await deleteAlbum(ids)
    selected.value = []
    emit('search')
    selectedAlbums.value = selectedAlbums.value.filter((item) => !ids.includes(item.id))
    albums.value = albums.value.filter((item) => !ids.includes(item.id))
    if (ids.includes(albumPreview.value.id)) {
      albumPreview.value = {
        id: 0,
        name: 'None',
        createTime: '',
        updateTime: '',
        user: null,
        photos: [],
        original: []
      }
      preview.value = {
        id: 0,
        name: 'None',
        albumId: 0,
        path: '',
        width: 1000,
        height: 1000,
        type: 0,
        thumbnail: '',
        exposureTime: 0,
        Gain: 0,
        createTime: ''
      }
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.delete = false
  }
}

// 显示更新
const showUpdate = () => {
  if (selected.value.length === 0) {
    return
  }
  const item = selected.value[0]
  update.value = {
    id: item.id,
    name: item.name
  }
  visible.value.update = true
}

// 更新
const handleUpdate = async () => {
  try {
    loading.value.update = true
    visible.value.update = false
    await updateAlbum(update.value)
    const album = selected.value.find((item) => item.id === update.value.id)
    if (album) {
      album.name = update.value.name
      emit('update', album)
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.update = false
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
