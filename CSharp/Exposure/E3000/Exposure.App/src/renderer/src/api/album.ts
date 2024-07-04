import axios from 'axios'
import { User } from './user'

// 图片
export interface Photo {
  id: number
  albumId: number
  name: string
  path: string
  width: number
  height: number
  type: number
  thumbnail: string
  exposureTime: number
  Gain: number
  createTime: string
}

export interface Album {
  id: number
  name: string
  createTime: string
  updateTime: string
  user: User | null
  photos: Photo[]
  original: Photo[]
}

export interface AlbumQueryParam {
  page: number
  size: number
  name: string
  startTime: string | null
  endTime: string | null
}

export interface AlbumPageResult {
  total: number
  list: Album[]
}

// 图片导出参数
export interface AlbumExportParam {
  ids: number[]
  format: string
}

// 图片调整参数
export interface PhotoAdjustParam {
  id: number
  brightness: number
  contrast: number
  invert: boolean
  code: number
}

// 图片更新参数
export interface APUpdateParam {
  id: number
  name: string
}

// 获取相册列表
export function getByPage(data: AlbumQueryParam) {
  return axios.post<AlbumPageResult>('/Album/Page', data)
}

// 导出
export function exportAlbum(data: AlbumExportParam) {
  return axios.post('/Album/Export', data)
}

// 合并图片
export function combinePhoto(data: number[]) {
  return axios.post<Photo | null>('/Photo/Combine', data)
}

// 调整图片
export function adjustPhoto(data: PhotoAdjustParam) {
  return axios.post('/Photo/Adjust', data)
}

// 删除图集
export function deleteAlbum(data: number[]) {
  return axios.delete('/Album', { data })
}

// 删除图片
export function deletePhoto(data: number[]) {
  return axios.delete('/Photo', { data })
}

// 更新图集
export function updateAlbum(data: APUpdateParam) {
  return axios.put('/Album', data)
}

//更新图片
export function updatePhoto(data: APUpdateParam) {
  return axios.put('/Photo', data)
}
