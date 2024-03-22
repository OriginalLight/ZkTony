import axios from 'axios'

// 图片
export interface Picture {
  id: number
  userId: number
  name: string
  path: string
  width: number
  height: number
  type: number
  thumbnail: string
  exposureTime: number
  exposureGain: number
  blackLevel: number
  isDelete: boolean
  createTime: string
  updateTime: string
  deleteTime: string
}

// 图片库
export interface PictureGallery {
  date: string
  light: Picture[]
  dark: Picture[]
  combine: Picture[]
}

// 图片参数
export interface PictureQueryParam {
  page: number
  size: number
  isDeleted: boolean
  name: string
  startTime: string | null
  endTime: string | null
}

// 图片分页结果
export interface PicturePageResult {
  total: number
  list: Picture[]
}

// 图片导出参数
export interface PictureExportParam {
  ids: number[]
  format: string
}

// 图片调整参数
export interface PictureAdjustParam {
  id: number
  brightness: number
  contrast: number
  invert: boolean
  code: number
}

// 图片更新参数
export interface PictureUpdateParam {
  id: number
  name: string
}

// 获取图片
export function getByPage(data: PictureQueryParam) {
  return axios.post<PicturePageResult>('/Picture/Page', data)
}

// 更新图片
export function updatePicture(data: PictureUpdateParam) {
  return axios.put('/Picture', data)
}

// 删除图片
export function deletePicture(data: number[]) {
  return axios.delete('/Picture', { data })
}

// 合并图片
export function combinePicture(data: number[]) {
  return axios.post<Picture>('/Picture/Combine', data)
}

// 导出图片
export function exportPicture(data: PictureExportParam) {
  return axios.post('/Picture/Export', data)
}

// 调整图片
export function adjustPicture(data: PictureAdjustParam) {
  return axios.post('/Picture/Adjust', data)
}
