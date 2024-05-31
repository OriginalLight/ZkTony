import { ref } from 'vue'
import { Album, Photo } from '@renderer/api/album'

// 预览
const preview = ref<Photo>({
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
})

const albumPreview = ref<Album>({
  id: 0,
  name: 'None',
  createTime: '',
  updateTime: '',
  user: null,
  photos: [],
  original: []
})

// 缩略图
const albums = ref<Album[]>([])

// 选中的图片
const selectedPhotos = ref<Photo[]>([])

// 选中的相册
const selectedAlbums = ref<Album[]>([])

// 选项
const options = ref({
  mode: 'auto',
  quality: '0',
  time: {
    minute: 0,
    second: 5,
    millisecond: 0
  },
  frame: 1
})

// 是否初始化
const isInit = ref(false)

// 初始化
const init = () => {
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
  albums.value = []
  selectedAlbums.value = []
  selectedPhotos.value = []
  options.value = {
    mode: 'auto',
    quality: '0',
    time: {
      minute: 0,
      second: 5,
      millisecond: 0
    },
    frame: 1
  }
  albumPreview.value = {
    id: 0,
    name: 'None',
    createTime: '',
    updateTime: '',
    user: null,
    photos: [],
    original: []
  }
}

export default function useHomeState() {
  return {
    preview,
    albums,
    selectedAlbums,
    selectedPhotos,
    albumPreview,
    options,
    isInit,
    init
  }
}
