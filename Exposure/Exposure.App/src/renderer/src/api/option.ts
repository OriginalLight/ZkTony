import axios from 'axios'

export interface OptionGetParam {
  key: string
}

export interface OptionSetParam {
  key: string
  value: string
}

export function getOption(data: OptionGetParam) {
  return axios.get<string>('/Option', { params: data })
}

export function setOption(data: OptionSetParam) {
  return axios.post('/Option', data)
}
