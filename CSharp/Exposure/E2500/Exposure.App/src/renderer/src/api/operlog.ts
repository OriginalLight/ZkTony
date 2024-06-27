import axios from 'axios'
import { UserState } from '@renderer/store/modules/user/types'

// 操作日志
export interface OperLog {
  id: number
  user: UserState | null
  type: string
  description: string
  time: string
}

// 操作日志查询
export interface OperLogQuery {
  page: number
  size: number
  date: string | null
}

// 操作日志分页结果
export interface OperLogPageResult {
  total: number
  list: OperLog[]
}

// 获取操作日志
export function getOperLogByPage(data: OperLogQuery) {
  return axios.post<OperLogPageResult>('/OperLog/Page', data)
}

// 删除操作日志
export function deleteOperLog(data: number[]) {
  return axios.delete('/OperLog', { data })
}

// 导出操作日志
export function exportOperLog(data: number[]) {
  return axios.post('/OperLog/Export', data)
}
