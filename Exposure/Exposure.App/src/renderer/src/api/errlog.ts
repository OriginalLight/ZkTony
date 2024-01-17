import axios from 'axios'

export interface ErrLog {
  id: number
  message: string
  type: string
  stackTrace: string
  source: string
  time: string
}

export interface ErrLogQuery {
  page: number
  size: number
  date: string | null
}

export interface ErrLogPageResult {
  total: number
  list: ErrLog[]
}

export function getErrLogByPage(data: ErrLogQuery) {
  return axios.post<ErrLogPageResult>('/ErrorLog/Page', data)
}

export function deleteErrLog(data: number[]) {
  return axios.delete('/ErrorLog', { data })
}

export function exportErrLog(data: number[]) {
  return axios.post('/ErrorLog/Export', data)
}
