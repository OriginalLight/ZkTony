import axios from 'axios'

export interface AgingParam {
  hatch: boolean
  light: boolean
  camera: boolean
  led: boolean
}

export function agingTest(data: AgingParam) {
  return axios.post('/Test/AgingTest', data)
}
