import axios from 'axios'

export interface LedParam {
  code: number
}

export interface SerialPortParam {
  port: string
  hex: string
}

export function led(data: LedParam) {
  return axios.get('/Debug/Led', { params: data })
}

export function serialPortStatus() {
  return axios.get<string[]>('/Debug/SerialPort')
}

export function serialPort(data: SerialPortParam) {
  return axios.post('/Debug/SerialPort', data)
}
