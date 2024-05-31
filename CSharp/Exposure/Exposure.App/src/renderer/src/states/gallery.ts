import { reactive, ref } from 'vue'
import { PaginationProps } from '@arco-design/web-vue'
import { Album, Photo } from '@renderer/api/album'

// 缩略图选中
const selected = ref<Album[]>([])

// 选项
const options = ref({
  name: '',
  date: []
})

const paginationProps = reactive<PaginationProps>({
  current: 1,
  defaultPageSize: 100,
  total: 0,
  showTotal: true
})

const subpage = ref<{
  album: Album
  selected: Photo[]
  preview: Photo
}>({
  album: {
    id: 0,
    name: '',
    user: null,
    photos: [],
    original: [],
    createTime: '',
    updateTime: ''
  },
  selected: [],
  preview: {
    id: 0,
    name: '',
    albumId: 0,
    path: '',
    width: 0,
    height: 0,
    type: 0,
    thumbnail: '',
    exposureTime: 0,
    Gain: 0,
    createTime: ''
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
