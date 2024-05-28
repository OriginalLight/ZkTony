import { resolve } from 'path'
import { defineConfig, externalizeDepsPlugin } from 'electron-vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  main: {
    plugins: [externalizeDepsPlugin()],
    envPrefix: 'MAIN_VITE_'
  },
  preload: {
    plugins: [externalizeDepsPlugin()],
    envPrefix: 'PRELOAD_VITE_'
  },
  renderer: {
    resolve: {
      alias: {
        '@renderer': resolve('src/renderer/src')
      }
    },
    plugins: [vue()],
    envPrefix: 'RENDERER_VITE_'
  }
})
