import axios from 'axios'

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

export interface PictureGallery {
  date: string
  light: Picture[]
  dark: Picture[]
  combine: Picture[]
}

export interface PictureQueryParam {
  page: number
  size: number
  isDeleted: boolean
  name: string
  startTime: string | null
  endTime: string | null
}

export interface PicturePageResult {
  total: number
  list: Picture[]
}

export interface PictureExportParam {
  ids: number[]
  format: string
}

export interface PictureAdjustParam {
  id: number
  brightness: number
  contrast: number
  invert: boolean
}

export interface PictureUpdateParam {
  id: number
  name: string
}

export function getByPage(data: PictureQueryParam) {
  return axios.post<PicturePageResult>('/Picture/Page', data)
}

export function updatePicture(data: PictureUpdateParam) {
  return axios.put('/Picture', data)
}

export function deletePicture(data: number[]) {
  return axios.delete('/Picture', { data })
}

export function combinePicture(data: number[]) {
  return axios.post<Picture>('/Picture/Combine', data)
}

export function exportPicture(data: PictureExportParam) {
  return axios.post('/Picture/Export', data)
}

export function adjustPicture(data: PictureAdjustParam) {
  return axios.post('/Picture/Adjust', data)
}
