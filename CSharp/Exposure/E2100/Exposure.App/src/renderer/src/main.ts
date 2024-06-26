import { createApp } from 'vue'
import ArcoVue from '@arco-design/web-vue'
import ArcoVueIcon from '@arco-design/web-vue/es/icon'
import App from './App.vue'
import router from './router'
import store from './store'
import i18n from './locale'
import VueLazyLoad from 'vue3-lazyload'
import { Message } from '@arco-design/web-vue'
import '@arco-themes/vue-zktony/index.less'
import '@renderer/api/interceptor'
import '@icon-park/vue-next/styles/index.css'

const app = createApp(App)
Message._context = app._context
app.use(ArcoVue)
app.use(ArcoVueIcon)
app.use(router)
app.use(store)
app.use(i18n)
app.use(VueLazyLoad, {})

app.mount('#app')
