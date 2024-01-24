import axios from 'axios'

export interface Status {
  usb: boolean
  door: boolean
  temperature: number
}

export function metric() {
  return axios.get<Status>('/Metric')
}
