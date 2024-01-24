import axios from 'axios'
import { UserState } from '@renderer/store/modules/user/types'

export interface User {
  id: number
  name: string
  role: number
  enabled: boolean
  createTime: string
  updateTime: string
  lastLoginTime: string
}

export interface LoginData {
  userName: string
  password: string
}

export interface UserQuery {
  page: number
  size: number
  name: string
}

export interface UserUpdateParam {
  id: number
  name: string
  oldPassword: string
  newPassword: string
  role: number
  enabled: boolean
}

export interface UserAddParam {
  name: string
  password: string
  role: number
  enabled: boolean
}

export interface UserPageResult {
  total: number
  list: User[]
}

export function login(data: LoginData) {
  return axios.post<UserState>('/User/Login', data)
}

export function logout() {
  return axios.get('/User/Logout')
}

export function getUserByPage(data: UserQuery) {
  return axios.post<UserPageResult>('/User/Page', data)
}

export function updateUser(data: UserUpdateParam) {
  return axios.put('/User', data)
}

export function addUser(data: UserAddParam) {
  return axios.post('/User', data)
}

export function deleteUser(data: number[]) {
  return axios.delete('/User', { data })
}
