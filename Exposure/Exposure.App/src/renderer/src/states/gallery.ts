import { reactive, ref } from 'vue'
import { Picture, PictureGallery } from '@renderer/api/picture'
import { PaginationProps } from '@arco-design/web-vue'

// 缩略图选中
const selected = ref<Picture[]>([])

// 选项
const options = ref({
  name: '',
  date: []
})

const paginationProps = reactive<PaginationProps>({
  current: 1,
  defaultPageSize: 200,
  total: 0,
  showTotal: true
})

const subpage = ref<{
  list: PictureGallery
  item: Picture
}>({
  list: {
    date: '',
    light: [],
    dark: [],
    combine: []
  },
  item: {
    id: 0,
    userId: 0,
    name: 'None',
    path: '',
    width: 1000,
    height: 1000,
    type: 0,
    thumbnail: '',
    exposureTime: 0,
    exposureGain: 0,
    blackLevel: 0,
    isDelete: false,
    createTime: '',
    updateTime: '',
    deleteTime: ''
  }
})

export default function useGalleryState() {
  return {
    selected,
    options,
    paginationProps,
    subpage
  }
}
