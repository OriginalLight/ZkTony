import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import App from './App.vue'
import router from './router'
import store from './store'
import i18n from './locale'
import Vue3Dragscroll from 'vue3-dragscroll'
import { Message } from '@arco-design/web-vue'
import '@arco-design/web-vue/dist/arco.css'
import '@renderer/api/interceptor'
import '@icon-park/vue-next/styles/index.css'

const app = createApp(App)
Message._context = app._context
app.use(ArcoVue)
app.use(ArcoVueIcon)
app.use(router)
app.use(store)
app.use(i18n)
app.use(Vue3Dragscroll)
app.mount('#app')
