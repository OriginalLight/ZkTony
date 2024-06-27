import axios from 'axios'

// 选项获取参数
export interface OptionGetParam {
  key: string
}

// 选项设置参数
export interface OptionSetParam {
  key: string
  value: string
}

// 获取选项
export function getOption(data: OptionGetParam) {
  return axios.get<string>('/Option', { params: data })
}

// 设置选项
export function setOption(data: OptionSetParam) {
  return axios.post('/Option', data)
}

// 获取所有选项
export function getAllOptions() {
  return axios.get<Record<string, string>>('/Option/All')
}
