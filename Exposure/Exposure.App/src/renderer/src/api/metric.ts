import axios from 'axios'

// 状态
export interface Status {
  usb: boolean
  hatch: boolean
  temperature: number
}

// 获取状态
export function metric() {
  return axios.get<Status>('/Metric')
}
