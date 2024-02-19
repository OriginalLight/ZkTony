import axios from 'axios'

export interface LedParam {
  code: number
}

export function led(data: LedParam) {
  return axios.get('/Debug/Led', { params: data })
}
