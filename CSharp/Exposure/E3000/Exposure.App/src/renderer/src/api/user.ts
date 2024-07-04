import axios from 'axios'
import { UserState } from '@renderer/store/modules/user/types'

// 用户
export interface User {
  id: number
  name: string
  role: number
  enabled: boolean
  createTime: string
  updateTime: string
  lastLoginTime: string
}

// 登录参数
export interface LoginData {
  userName: string
  password: string
}

// 用户查询参数
export interface UserQueryParam {
  page: number
  size: number
  name: string
}

// 用户更新参数
export interface UserUpdateParam {
  id: number
  name: string
  oldPassword: string
  newPassword: string
  role: number
  enabled: boolean
}

// 用户添加参数
export interface UserAddParam {
  name: string
  password: string
  role: number
  enabled: boolean
}

// 用户分页结果
export interface UserPageResult {
  total: number
  list: User[]
}

// 获取用户
export function login(data: LoginData) {
  return axios.post<UserState>('/User/Login', data)
}

// 退出登录
export function logout() {
  return axios.get('/User/Logout')
}

// 获取用户
export function getUserByPage(data: UserQueryParam) {
  return axios.post<UserPageResult>('/User/Page', data)
}

// 更新用户
export function updateUser(data: UserUpdateParam) {
  return axios.put('/User', data)
}

// 添加用户
export function addUser(data: UserAddParam) {
  return axios.post('/User', data)
}

// 删除用户
export function deleteUser(data: number[]) {
  return axios.delete('/User', { data })
}

// 导出用户
export function getUserById(data: number) {
  return axios.get<User>('/User?id=' + data)
}
