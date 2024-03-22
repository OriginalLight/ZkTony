import axios from 'axios'

// 老化测试参数
export interface AgingParam {
  hatch: boolean
  light: boolean
  camera: boolean
  led: boolean
  cycle: number
  interval: number
}

// 老化测试
export function agingTest(data: AgingParam) {
  return axios.post('/Test/AgingTest', data)
}
