import axios from 'axios'

// 串口参数
export interface CodeParam {
  code: number
}

// 串口参数
export interface SerialPortParam {
  port: string
  hex: string
}

// 机器信息
export interface MachineInfo {
  id: string
  version: string
}

// 获取机器信息
export function getPorts() {
  return axios.get<string[]>('/Machine/SerialPort')
}

// 串口
export function serialPort(data: SerialPortParam) {
  return axios.post('/Machine/SerialPort', data)
}

// Led 灯
export function led(data: CodeParam) {
  return axios.get('/Machine/Led', { params: data })
}

// 电机
export function hatch(data: CodeParam) {
  return axios.get('/Machine/Hatch', { params: data })
}

// 闪光灯
export function light(data: CodeParam) {
  return axios.get('/Machine/Light', { params: data })
}

// 相机
export function camera(data: CodeParam) {
  return axios.get('/Machine/Camera', { params: data })
}

// 屏幕
export function screen(data: CodeParam) {
  return axios.get('/Machine/Screen', { params: data })
}

// 机器信息
export function varsion() {
  return axios.get<Record<string, string>>('/Machine/Version')
}

// 存储信息
export function storage() {
  return axios.get<number>('/Machine/Storage')
}

// 自检
export function selfCheck() {
  return axios.get('/Machine/SelfCheck')
}

// 升级
export function update() {
  return axios.get('/Machine/Update')
}

//关机
export function shutdown() {
  return axios.get('/Machine/Shutdown')
}
