import axios from 'axios'
import type { InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { Message } from '@arco-design/web-vue'

// 定义接口
export interface HttpResponse<T = unknown> {
  code: number
  msg: string
  data: T
}

// 设置基础路径
if (import.meta.env.RENDERER_VITE_API_BASE_URL) {
  axios.defaults.baseURL = import.meta.env.RENDERER_VITE_API_BASE_URL
}

// 请求拦截器
axios.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // let each request carry token
    // this example using the JWT token
    // Authorization is a custom headers key
    // please modify it according to the actual situation
    return config
  },
  (error) => {
    // do something
    Message.error(error.message)
    return Promise.reject(error)
  }
)

// 回复拦截器
axios.interceptors.response.use(
  (response: AxiosResponse<HttpResponse>) => {
    const res = response.data
    if (res.code !== 200) {
      Message.error(res.msg)
      return Promise.reject(res.msg)
    }
    return response
  },
  (error) => {
    // do something
    Message.error(error.message)
    return Promise.reject(error)
  }
)
