import axios from 'axios'

export interface Picture {
  id: number
  userId: number
  name: string
  path: string
  width: number
  height: number
  exposureTime: number
  exposureGain: number
  blackLevel: number
  isDelete: boolean
  createTime: string
  updateTime: string
  deleteTime: string
}

export interface PictureQuery {
  page: number
  size: number
  isDeleted: boolean
  name: string
  startTime: string
  endTime: string
}

export function getByPage(data: PictureQuery) {
  return axios.post<Picture[]>('/Picture/Page', data)
}

export function Update(data: Picture) {
  return axios.put('/Picture', data)
}

export function Delete(data: number[]) {
  return axios.delete('/Picture', { data })
}
