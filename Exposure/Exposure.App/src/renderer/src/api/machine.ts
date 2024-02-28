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

export function light(data: CodeParam) {
  return axios.get('/Machine/Light', { params: data })
}

export function camera(data: CodeParam) {
  return axios.get('/Machine/Camera', { params: data })
}

export function screen(data: CodeParam) {
  return axios.get('/Machine/Screen', { params: data })
}
