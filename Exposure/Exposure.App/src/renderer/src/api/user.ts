import axios from 'axios'
import { UserState } from '@renderer/store/modules/user/types'

export interface LoginData {
  userName: string
  password: string
}

export function login(data: LoginData) {
  return axios.post<UserState>('/User/Login', data)
}

export function logout() {
  return axios.get('/User/Logout')
}
