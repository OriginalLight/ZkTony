import axios from 'axios'
import { Picture } from './picture'

export interface PixelParam {
  index: number
}

export interface ManualParam {
  exposure: number
  frame: number
}

export function init() {
  return axios.get('/Camera/Init')
}

export function preview() {
  return axios.get<Picture>('/Camera/Preview')
}

// url传参index
export function pixel(data: PixelParam) {
  return axios.get('/Camera/Pixel', { params: data })
}

export function auto() {
  return axios.get('/Camera/Auto')
}

export function manual(data: ManualParam) {
  return axios.get('/Camera/Manual', { params: data })
}

export function cancel() {
  return axios.get('/Camera/Cancel')
}

export function cache() {
  return axios.get<Picture[]>('/Camera/Cache')
}
