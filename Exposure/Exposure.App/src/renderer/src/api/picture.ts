import axios from 'axios'

export interface Picture {
  id: number
  userId: number
  name: string
  path: string
  width: number
  height: number
  type: number
  exposureTime: number
  exposureGain: number
  blackLevel: number
  isDelete: boolean
  createTime: string
  updateTime: string
  deleteTime: string
}

export interface PictureQueryParam {
  page: number
  size: number
  isDeleted: boolean
  name: string
  startTime: string
  endTime: string
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
}

export function getByPage(data: PictureQueryParam) {
  return axios.post<PicturePageResult>('/Picture/Page', data)
}

export function Update(data: Picture) {
  return axios.put('/Picture', data)
}

export function Delete(data: number[]) {
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
