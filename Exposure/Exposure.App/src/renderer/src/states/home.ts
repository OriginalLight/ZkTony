import { ref } from 'vue'
import { Picture } from '@renderer/api/picture'

// 预览
const preview = ref<Picture>({
  id: 0,
  userId: 0,
  name: 'None',
  path: 'None',
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
})

// 缩略图
const thumbnail = ref<Picture[]>([])

// 缩略图选中
const selected = ref<Picture[]>([])

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
  thumbnail.value = []
  selected.value = []
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
}

export default function useHomeState() {
  return {
    preview,
    thumbnail,
    selected,
    options,
    isInit,
    init
  }
}
