<template>
  <a-list
    class="list"
    :scrollbar="true"
    :loading="loading"
    :data="list"
    :max-height="maxHeight"
    :pagination-props="paginationProps"
    @page-change="handlePageChange"
  >
    <template #item="{ item }">
      <a-list-item action-layout="vertical">
        <a-space>
          <a-tag size="large">{{ item.date }}</a-tag>
          <a-button shape="round" style="width: 100px" @click="handleSelectedAll(item.albums)">
            {{ selectedNumber(item.albums) }}
          </a-button>
        </a-space>
        <a-divider />
        <a-space wrap>
          <div
            v-for="(album, index) in item.albums"
            :key="album.id"
            class="scroll-item"
            @click="handleSelected(album)"
            @dblclick="handleDetail(album)"
            @longClick="handleSelected(album)"
          >
            <div
              v-for="(photo, index1) in album.photos"
              :key="photo.id"
              class="album-img-box"
              :style="{
                top: (20 / (album.photos.length - 1)) * index1 + 'px',
                left: (20 / (album.photos.length - 1)) * index1 + 'px'
              }"
            >
              <img v-lazy="photo.thumbnail" class="album-img" alt="dessert" />
            </div>
            <div v-if="showSelected(album)" class="selected">√</div>
            <div class="img-index">{{ index + 1 }}</div>
            <div class="img-name">{{ album.name }}</div>
          </div>
        </a-space>
      </a-list-item>
    </template>
  </a-list>
</template>

<script lang="ts" setup>
import { ref, computed, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { useWindowSize } from '@vueuse/core'
import useGalleryState from '@renderer/states/gallery'
import { Message } from '@arco-design/web-vue'
import { Album, getByPage } from '@renderer/api/album'
import dayjs from 'dayjs'

const router = useRouter()
// 窗口高度
const { height } = useWindowSize()
// 最大高度
const maxHeight = computed(() => {
  return height.value - 200
})
// 分页参数
const { selected, options, paginationProps, subpage } = useGalleryState()
// 加载状态
const loading = ref(false)
// 图片列表
const data = ref<Album[]>([])
// data 处理
const list = computed<{ date: string; albums: Album[] }[]>(() => {
  const map = new Map<string, Album[]>()
  data.value.forEach((item) => {
    const date = dayjs(item.createTime).format('YYYY-MM-DD')
    if (map.has(date)) {
      map.get(date)?.push(item)
    } else {
      map.set(date, [item])
    }
  })
  return Array.from(map).map(([date, albums]) => {
    return {
      date,
      albums
    }
  })
})
// 是否显示选中
const showSelected = (item: Album) => {
  return selected.value.some((selected) => selected.id === item.id)
}
// 选择
const handleSelected = (item: Album) => {
  if (selected.value.find((selected) => selected.id === item.id)) {
    selected.value = selected.value.filter((selected) => selected.id !== item.id)
  } else {
    selected.value.push(item)
  }
}
// 全选或者取消全选
const handleSelectedAll = (list: Album[]) => {
  if (list.every((item) => selected.value.find((selected) => selected.id === item.id))) {
    selected.value = selected.value.filter(
      (selected) => !list.find((item) => item.id === selected.id)
    )
  } else {
    selected.value = selected.value.concat(list)
  }
}

const selectedNumber = (albums: Album[]) => {
  // list中被选中的
  const num = albums.filter((item) => selected.value.find((selected) => selected.id === item.id))
  return num.length + ' / ' + albums.length
}
// 分页
const handlePageChange = (page: number) => {
  paginationProps.current = page
  fetchData()
}
// 详情
const handleDetail = (item: Album) => {
  if (item.photos.length === 0) {
    return
  }
  subpage.value = {
    album: item,
    selected: [],
    preview: item.photos.find((photo) => photo.type === 1) ?? item.photos[0]
  }
  router.push('/gallery-detail')
}

// 加载数据
const fetchData = async () => {
  try {
    loading.value = true
    const res = await getByPage({
      page: paginationProps.current ? paginationProps.current : 1,
      size: paginationProps.defaultPageSize ? paginationProps.defaultPageSize : 200,
      name: options.value.name,
      startTime: options.value.date ? options.value.date[0] : null,
      endTime: options.value.date ? options.value.date[1] : null
    })
    data.value = res.data.list
    paginationProps.total = res.data.total
  } catch (error) {
    Message.error((error as Error).message)
  } finally {
    loading.value = false
  }
}
// 暴露方法
defineExpose({
  fetchData
})
// 初始化
onActivated(async () => {
  fetchData()
})
</script>

<style scoped lang="less">
.list {
  flex: 1;
  border-radius: 2px;
  margin-top: 8px;
  overflow: hidden;
  background: var(--color-bg-2);
}
.box::-webkit-scrollbar {
  display: none;
}

:deep(.arco-list-pagination) {
  float: right;
  margin-top: 12px;
}

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
</style>
