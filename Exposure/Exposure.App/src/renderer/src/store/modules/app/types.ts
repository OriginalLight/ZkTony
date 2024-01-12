export interface AppState {
  device: string
  usb: boolean
  door: boolean
  temperature: number
  [key: string]: unknown
}
