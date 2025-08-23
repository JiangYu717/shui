import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器（不再处理 token）
service.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    // 如果响应为空，返回错误
    if (!response) {
      return Promise.reject(new Error('服务器响应为空'))
    }
    // 如果是blob类型的响应（文件下载），直接返回response
    if (response.config.responseType === 'blob') {
      return response
    }
    // 如果响应数据为空，只在GET/POST且不是删除等操作时报错
    const method = response.config.method && response.config.method.toLowerCase()
    if ((method === 'get' || method === 'post') && (response.data === null || response.data === undefined)) {
      return Promise.reject(new Error('服务器响应数据为空'))
    }
    // 返回实际的响应数据
    return {
      data: response.data,
      status: response.status,
      headers: response.headers
    }
  },
  error => {
    console.error('响应错误:', error)
    let message = '网络错误'
    if (error.response) {
      const status = error.response.status
      const responseData = error.response.data
      switch (status) {
        case 403:
          message = responseData?.message || '拒绝访问'
          break
        case 404:
          message = responseData?.message || '请求错误，未找到该资源'
          break
        case 500:
          message = responseData?.message || '服务器错误'
          break
        default:
          message = responseData?.message || `请求失败(${status})`
      }
    } else if (error.request) {
      message = '服务器无响应'
    } else if (error.message) {
      message = error.message
    }
    ElMessage({
      message: message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service 