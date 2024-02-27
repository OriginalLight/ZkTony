import axios from 'axios'

export interface CodeParam {
  code: number
}

export interface SerialPortParam {
  port: string
  hex: string
}

export function serialPortStatus() {
  return axios.get<string[]>('/Machine/SerialPort')
}

export function serialPort(data: SerialPortParam) {
  return axios.post('/Machine/SerialPort', data)
}

export function led(data: CodeParam) {
  return axios.get('/Machine/Led', { params: data })
}

export function hatch(data: CodeParam) {
  return axios.get('/Machine/Hatch', { params: data })
}
