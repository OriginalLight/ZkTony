import axios from 'axios'

// 选项获取参数
export interface AudioParam {
  key: string
}

// 获取选项
export function play(data: AudioParam) {
  return axios.get('/Audio/Play', { params: data })
}
