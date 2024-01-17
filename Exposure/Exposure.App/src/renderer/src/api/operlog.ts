import axios from 'axios'
import { UserState } from '@renderer/store/modules/user/types'

export interface OperLog {
  id: number
  user: UserState | null
  type: string
  description: string
  time: string
}

export interface OperLogQuery {
  page: number
  size: number
  date: string | null
}

export interface OperLogPageResult {
  total: number
  list: OperLog[]
}

export function getOperLogByPage(data: OperLogQuery) {
  return axios.post<OperLogPageResult>('/OperLog/Page', data)
}

export function deleteOperLog(data: number[]) {
  return axios.delete('/OperLog', { data })
}

export function exportOperLog(data: number[]) {
  return axios.post('/OperLog/Export', data)
}
