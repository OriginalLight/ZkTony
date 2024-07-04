import { defineStore } from 'pinia'
import { login as userLogin, logout as userLogout, LoginData } from '@renderer/api/user'
import { UserState } from './types'

const useUserStore = defineStore('user', {
  state: (): UserState => ({
    id: 0,
    name: 'zkty',
    role: 2,
    enabled: false,
    createTime: '',
    updateTime: '',
    lastLoginTime: ''
  }),

  persist: true,

  getters: {
    userInfo(state: UserState): UserState {
      return { ...state }
    }
  },

  actions: {
    // Set user's information
    setInfo(partial: Partial<UserState>) {
      this.$patch(partial)
    },

    // Reset user's information
    resetInfo() {
      this.$reset()
    },

    // Login
    async login(loginForm: LoginData) {
      try {
        const res = await userLogin(loginForm)
        this.setInfo(res.data)
      } catch (err) {
        this.resetInfo()
        throw err
      }
    },
    logoutCallBack() {
      this.resetInfo()
    },
    // Logout
    async logout() {
      try {
        await userLogout()
      } finally {
        this.logoutCallBack()
      }
    }
  }
})

export default useUserStore
