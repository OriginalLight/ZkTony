import { defineStore } from 'pinia'
import { AppState } from './types'

const useAppStore = defineStore('app', {
  state: (): AppState => ({
    device: 'desktop',
    usb: false,
    door: false,
    temperature: 0
  }),

  persist: true,

  getters: {
    appCurrentSetting(state: AppState): AppState {
      return { ...state }
    },
    appDevice(state: AppState) {
      return state.device
    },
    appKey(state: AppState) {
      return state.key
    }
  },

  actions: {
    // Update app settings
    updateSettings(partial: Partial<AppState>) {
      // @ts-ignore-next-line
      this.$patch(partial)
    },
    toggleDevice(device: string) {
      this.device = device
    },
    toggleUsb(usb: boolean) {
      this.usb = usb
    },
    toggleDoor(door: boolean) {
      this.door = door
    },
    toggleTemperature(temperature: number) {
      this.temperature = temperature
    },
    toggleKey(key: string) {
      this.key = key
    }
  }
})

export default useAppStore
