<template>
  <a-card
    class="card"
    :body-style="{
      height: '100%',
      display: 'flex',
      flexDirection: 'column',
      justifyContent: 'space-between'
    }"
  >
    <div style="display: flex; justify-content: space-between">
      <a-space>
        <a-tag style="width: 100px">
          <div style="width: 100%; text-align: center">
            {{ t('home.album.view.album') }}
          </div>
        </a-tag>
        <a-tag style="width: 40px">
          <div style="width: 100%; text-align: center">{{ albumList.length }}</div>
        </a-tag>
      </a-space>
      <a-space>
        <a-button
          type="primary"
          shape="round"
          style="width: 120px"
          :loading="loading.update"
          :disabled="disableUpdate"
          @click="showUpdate"
        >
          <template #icon>
            <icon-edit />
          </template>
          <template #default>{{ t('gallery.bar.rename') }}</template>
        </a-button>
        <a-popover position="br" trigger="click">
          <a-button
            type="primary"
            style="width: 120px"
            shape="round"
            :disabled="disableExport"
            :loading="loading.export"
          >
            <template #icon>
              <icon-export />
            </template>
            {{ t('home.album.view.export') }}</a-button
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
    <a-space id="scroll-albums" class="scroll-wrapper">
      <div
        v-for="(item, index) in albumList"
        :key="item.id"
        class="scroll-item"
        @click="emit('clickAlbum', item)"
      >
        <div
          v-for="(photo, index1) in item.photos"
          :key="photo.id"
          class="album-img-box"
          :style="{
            top: (20 / (item.photos.length - 1)) * index1 + 'px',
            left: (20 / (item.photos.length - 1)) * index1 + 'px'
          }"
        >
          <img v-lazy="photo.thumbnail" class="album-img" alt="dessert" />
        </div>
        <div v-if="showSelectedAlbum(item)" class="selected">√</div>
        <div v-if="showPreviewAlbum(item)" class="img-preview">
          {{ t('home.album.view.preview') }}
        </div>
        <div class="img-index">{{ index + 1 }}</div>
        <div class="img-name">{{ item.name }}</div>
      </div>
    </a-space>
    <div v-show="albumPreview.id != 0" style="display: flex; justify-content: space-between">
      <a-space>
        <a-tag style="width: 150px">
          <div style="width: 100%; text-align: center">
            {{ albumPreview.name }}
          </div>
        </a-tag>
        <a-tag style="width: 40px">
          <div style="width: 100%; text-align: center">{{ albumPreview.photos.length }}</div>
        </a-tag>
      </a-space>
      <a-button
        type="primary"
        style="width: 120px"
        shape="round"
        :disabled="diableCombine"
        :loading="loading.combine"
        @click="handleCombine"
      >
        <template #icon>
          <icon-export />
        </template>
        {{ t('home.album.view.combine') }}</a-button
      >
    </div>
    <a-space v-show="albumPreview.id != 0" id="scroll-img" class="scroll-wrapper-img">
      <div
        v-for="(item, index) in albumPreview.photos"
        :key="item.id"
        class="scroll-item"
        @click="emit('clickPhoto', item)"
      >
        <img v-lazy="item.thumbnail" class="img" alt="dessert" />
        <div v-if="showPreviewPhoto(item)" class="img-preview">
          {{ t('home.album.view.preview') }}
        </div>
        <div v-if="showSelectedPhoto(item)" class="selected">√</div>
        <div class="img-index">{{ index + 1 }}</div>
        <div class="img-name">{{ item.name }}</div>
      </div>
    </a-space>
  </a-card>
  <a-modal v-model:visible="visible" draggable @ok="handleUpdate" @cancel="visible = false">
    <template #title> {{ t('gallery.bar.rename') }} </template>
    <a-input v-model="update.name" allow-clear :max-length="32"> </a-input>
  </a-modal>
</template>

<script lang="ts" setup>
import { computed, ref, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { Album, Photo, combinePhoto, exportAlbum, updateAlbum } from '@renderer/api/album'
import { Message } from '@arco-design/web-vue'

const props = defineProps({
  preview: {
    type: Object as () => Photo,
    required: true
  },
  albums: {
    type: Object as () => Album[],
    required: true
  },
  selectedAlbums: {
    type: Object as () => Album[],
    required: true
  },
  selectedPhotos: {
    type: Object as () => Photo[],
    required: true
  },
  albumPreview: {
    type: Object as () => Album,
    required: true
  }
})
const emit = defineEmits(['clickAlbum', 'clickPhoto', 'combine'])
const { t } = useI18n()

// 加载
const loading = ref({
  combine: false,
  export: false,
  update: false
})

// 更新
const update = ref({
  id: 0,
  name: ''
})

const visible = ref(false)

const albumList = computed(() => {
  // 返回album并按时间倒着排序
  return props.albums
})

// 是否禁用合并
const diableCombine = computed(() => {
  // 数量等于2, 类型一个是白光一个是曝光, 不能同时是白光或者曝光
  return (
    props.selectedPhotos.length !== 2 ||
    !(
      props.selectedPhotos.some((item) => item.type === 0) &&
      props.selectedPhotos.some((item) => item.type === 1)
    )
  )
})

// 是否禁用导出
const disableExport = computed(() => {
  // 数量等于0
  return props.selectedAlbums.length === 0 || loading.value.export
})

// 导出
const handleExport = async (format: string) => {
  try {
    loading.value.export = true
    await exportAlbum({
      ids: props.selectedAlbums.map((item) => item.id),
      format
    })
    Message.success(t('home.album.view.export.success'))
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.export = false
  }
}

// 合并
const handleCombine = async () => {
  try {
    loading.value.combine = true
    const res = await combinePhoto(props.selectedPhotos.map((item) => item.id))
    emit('combine', res.data)
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.combine = false
  }
}

// 是否显示预览图集
const showPreviewAlbum = (item: Album) => {
  return item.photos.some((photo) => photo.id === props.preview.id)
}

// 是否显示预览图片
const showPreviewPhoto = (item: Photo) => {
  return item.id === props.preview.id
}

// 是否选中图集
const showSelectedAlbum = (item: Album) => {
  return props.selectedAlbums.some((album) => album.id === item.id)
}

// 是否选中照片
const showSelectedPhoto = (item: Photo) => {
  return props.selectedPhotos.some((photo) => photo.id === item.id)
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

// 是否禁用更新
const disableUpdate = computed(() => {
  return props.selectedAlbums.length != 1 || loading.value.update
})

// 显示更新
const showUpdate = () => {
  if (props.selectedAlbums.length === 0) {
    return
  }
  const item = props.selectedAlbums[0]
  update.value = {
    id: item.id,
    name: item.name
  }
  visible.value = true
}

// 更新
const handleUpdate = async () => {
  try {
    loading.value.update = true
    visible.value = false
    await updateAlbum(update.value)
    const album = props.selectedAlbums.find((item) => item.id === update.value.id)
    if (album) {
      album.name = update.value.name
    }
    const album1 = props.albums.find((item) => item.id === update.value.id)
    if (album1) {
      album1.name = update.value.name
    }
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value.update = false
  }
}

onMounted(() => {
  mouseWhell('scroll-albums')
  mouseWhell('scroll-img')
})
</script>

<style scoped lang="less">
.card {
  display: flex;
  flex-direction: column;
  height: 100%;
  margin-top: 8px;
  border-radius: 4px;
}

.scroll-wrapper {
  display: flex;
  height: 100px;
  overflow-y: hidden;
  overflow-x: scroll;

  .scroll-item {
    display: flex;
    position: relative;
    width: 100px;
    height: 100px;

    .img-preview {
      position: absolute;
      top: 0;
      left: 0;
      font-size: 10px;
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

    .album-img-box {
      position: absolute;

      .album-img {
        height: 80px;
        min-height: 80px;
        object-fit: contain;
        border-radius: 2px;
      }
    }
    .img-index {
      position: absolute;
      top: 0;
      right: 0;
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
      color: var(--color-bg-2);
      font-size: 10px;
      text-align: center;
      overflow: hidden;
      border-radius: 0px 0px 2px 2px;
      background-color: rgb(var(--primary-6));
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.scroll-wrapper-img {
  display: flex;
  height: 200px;
  overflow-y: hidden;
  overflow-x: scroll;

  .scroll-item {
    display: flex;
    position: relative;

    .img-preview {
      position: absolute;
      top: 0;
      left: 0;
      font-size: 12px;
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
      font-size: 36px;
      font-weight: bold;
      color: rgb(var(--primary-6));
      user-select: none;
    }

    .img {
      height: 160px;
      min-height: 160px;
      width: auto;
      object-fit: contain;
      border-radius: 2px;
    }

    .img-index {
      position: absolute;
      top: 4px;
      right: 4px;
      color: var(--color-bg-2);
      font-size: 16px;
      text-align: center;
      overflow: hidden;
      color: rgb(var(--primary-6));
    }

    .img-name {
      position: absolute;
      bottom: 0;
      width: 100%;
      color: var(--color-bg-2);
      font-size: 12px;
      text-align: center;
      overflow: hidden;
      border-radius: 0px 0px 2px 2px;
      background-color: rgb(var(--primary-6));
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.scroll-wrapper::-webkit-scrollbar {
  display: none;
}

.scroll-wrapper-img::-webkit-scrollbar {
  display: none;
}
</style>
