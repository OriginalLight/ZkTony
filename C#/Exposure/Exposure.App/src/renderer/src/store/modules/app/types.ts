export interface AppState {
  device: string
  usb: boolean
  hatch: boolean
  temperature: number
  [key: string]: unknown
}
