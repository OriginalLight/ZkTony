import axios from 'axios'

// 错误日志
export interface ErrLog {
  id: number
  message: string
  type: string
  stackTrace: string
  source: string
  time: string
}

// 错误日志查询
export interface ErrLogQuery {
  page: number
  size: number
  date: string | null
}

// 错误日志分页结果
export interface ErrLogPageResult {
  total: number
  list: ErrLog[]
}

// 获取错误日志
export function getErrLogByPage(data: ErrLogQuery) {
  return axios.post<ErrLogPageResult>('/ErrorLog/Page', data)
}

// 删除错误日志
export function deleteErrLog(data: number[]) {
  return axios.delete('/ErrorLog', { data })
}

// 导出错误日志
export function exportErrLog(data: number[]) {
  return axios.post('/ErrorLog/Export', data)
}
