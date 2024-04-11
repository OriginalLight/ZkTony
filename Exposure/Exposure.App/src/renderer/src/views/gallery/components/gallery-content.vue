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
          <a-tag size="large">{{
            item.light.length + item.dark.length + item.combine.length
          }}</a-tag>
          <a-button shape="round" @click="handleSelectedAll(item)">
            {{
              showSelectedAll(item)
                ? t('gallery.content.picture.cancelSelectAll')
                : t('gallery.content.picture.selectAll')
            }}
          </a-button>
        </a-space>
        <a-divider v-if="item.light.length > 0" orientation="right">{{
          t('gallery.content.picture.light') + ' - ' + item.light.length
        }}</a-divider>
        <a-space wrap>
          <div
            v-for="img in item.light"
            :key="img.id"
            class="img-box"
            @click="handleSelected(item, img)"
            @dblclick="handleDetail(item, img)"
          >
            <img v-lazy="img.thumbnail" class="img" />
            <icon-check v-if="showSelected(img)" class="selected" />
            <div class="img-name">{{ img.name }}</div>
          </div>
        </a-space>
        <a-divider v-if="item.dark.length > 0" orientation="right">{{
          t('gallery.content.picture.dark') + ' - ' + item.dark.length
        }}</a-divider>
        <a-space wrap>
          <div
            v-for="img in item.dark"
            :key="img.id"
            class="img-box"
            @click="handleSelected(item, img)"
            @dblclick="handleDetail(item, img)"
          >
            <img v-lazy="img.thumbnail" class="img" />
            <icon-check v-if="showSelected(img)" class="selected" />
            <div class="img-name">{{ img.name }}</div>
          </div>
        </a-space>
        <a-divider v-if="item.combine.length > 0" orientation="right">{{
          t('gallery.content.picture.combine') + ' - ' + item.combine.length
        }}</a-divider>
        <a-space wrap>
          <div
            v-for="img in item.combine"
            :key="img.id"
            class="img-box"
            @click="handleSelected(item, img)"
            @dblclick="handleDetail(item, img)"
          >
            <img v-lazy="img.thumbnail" class="img" />
            <icon-check v-if="showSelected(img)" class="selected" />
            <div class="img-name">{{ img.name }}</div>
          </div>
        </a-space>
      </a-list-item>
    </template>
  </a-list>
</template>

<script lang="ts" setup>
import { ref, computed, onActivated } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import { useWindowSize } from '@vueuse/core'
import { Picture, PictureGallery, getByPage } from '@renderer/api/picture'
import useGalleryState from '@renderer/states/gallery'
import { Message } from '@arco-design/web-vue'

const { t } = useI18n()

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
const data = ref<Picture[]>([])
// 图片列表
const list = computed<PictureGallery[]>(() => {
  // 根据日期分类同一天的放入一个数组并按照时间倒序排列并且按照type分类
  const map = new Map()
  data.value.forEach((item) => {
    const date = dayjs(item.createTime).format('YYYY-MM-DD')
    if (map.has(date)) {
      map.get(date).push(item)
    } else {
      map.set(date, [item])
    }
  })
  const arr = Array.from(map)
  arr.sort((a, b) => {
    return dayjs(b[0]).valueOf() - dayjs(a[0]).valueOf()
  })
  const gallery: PictureGallery[] = []

  arr.forEach((item) => {
    gallery.push({
      date: item[0],
      light: item[1].filter((item) => item.type === 0),
      dark: item[1].filter((item) => item.type === 1),
      combine: item[1].filter((item) => item.type === 2)
    })
  })

  return gallery
})
// 是否显示选中
const showSelected = (item: Picture) => {
  return selected.value.some((selected) => selected.id === item.id)
}
// 是否显示全部选中
const showSelectedAll = (list: PictureGallery) => {
  const item = list.light.concat(list.dark).concat(list.combine)
  return item.every((item) => selected.value.find((selected) => selected.id === item.id))
}
// 选择
const handleSelected = (list: PictureGallery, item: Picture) => {
  if (selected.value.find((selected) => selected.id === item.id)) {
    selected.value = selected.value.filter((selected) => selected.id !== item.id)
  } else {
    selected.value.push(item)
  }
  subpage.value = {
    list,
    item
  }
}
// 全选或者取消全选
const handleSelectedAll = (list: PictureGallery) => {
  const item = list.light.concat(list.dark).concat(list.combine)
  // item 全在选中列表中 则取消全选
  if (item.every((item) => selected.value.find((selected) => selected.id === item.id))) {
    selected.value = selected.value.filter(
      (selected) => !item.find((item) => item.id === selected.id)
    )
  } else {
    selected.value = selected.value.concat(item)
  }
}
// 分页
const handlePageChange = (page: number) => {
  paginationProps.current = page
  fetchData()
}
// 详情
const handleDetail = (list: PictureGallery, item: Picture) => {
  subpage.value = {
    list,
    item
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
      isDeleted: false,
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

.img-box {
  display: flex;
  position: relative;

  .img {
    width: 100px;
    height: 100px;
    object-fit: contain;
    border-radius: 4px;
  }

  .selected {
    position: absolute;
    top: 0;
    right: 0;
    color: var(--color-bg-2);
    border-radius: 2px;
    background-color: rgb(var(--primary-6));
  }

  .img-name {
    position: absolute;
    bottom: 0;
    width: 100%;
    border-radius: 4px;
    color: var(--color-bg-2);
    font-size: 10px;
    text-align: center;
    overflow: hidden;
    background-color: rgb(var(--primary-6));
  }
}
</style>
