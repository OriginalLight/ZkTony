import axios from 'axios'
import { Album, Photo } from './album'

export interface PixelParam {
  index: number
}

export interface ManualParam {
  exposure: number
  frame: number
}
// 初始化
export function init() {
  return axios.get('/Camera/Init')
}

// 预览
export function preview() {
  return axios.get<Photo>('/Camera/Preview')
}

// 设置画质
export function pixel(data: PixelParam) {
  return axios.get('/Camera/Pixel', { params: data })
}

// 自动曝光
export function auto() {
  return axios.get<number>('/Camera/Auto')
}

// 手动曝光
export function manual(data: ManualParam) {
  return axios.get('/Camera/Manual', { params: data })
}

// 取消
export function cancel() {
  return axios.get('/Camera/Cancel')
}

// 获取结果
export function result() {
  return axios.get<Album>('/Camera/Result')
}

// 导入
export function calibrate() {
  return axios.get('/Camera/Calibrate')
}
