import axios, { type AxiosInstance, type AxiosRequestConfig } from 'axios'
import { message } from 'ant-design-vue'

/**
 * 全局 axios 实例。
 * - 调用后端 /api/v1/* 接口（由 vite proxy 转发到 Spring Boot 8080）
 * - 后端不可用时，业务层会捕获错误并回退到本地 Mock 数据，保证页面可见
 */
const instance: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 8000,
  headers: {
    'Content-Type': 'application/json'
  }
})

instance.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('kg_token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

instance.interceptors.response.use(
  (response) => {
    const data = response.data
    if (data && typeof data === 'object' && 'code' in data) {
      if (data.code === 0 || data.code === 200) {
        return data.data ?? data
      }
      message.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return data
  },
  (error) => {
    // 网络错误 / 后端未启动：交给业务层降级处理，不弹 toast 避免刷屏
    if (error.code === 'ERR_NETWORK' || error.response?.status === 504) {
      // 后端未启动，使用 Mock 数据
      return Promise.reject(new Error('BACKEND_UNAVAILABLE'))
    }
    return Promise.reject(error)
  }
)

/**
 * 通用请求封装：失败时返回 fallback 值（通常由业务层传入 Mock 数据）
 */
export async function request<T = any>(
  config: AxiosRequestConfig,
  fallback?: T
): Promise<T> {
  try {
    return (await instance(config)) as T
  } catch (e: any) {
    if (fallback !== undefined) {
      // 静默回退到 Mock，保证页面可见
      return fallback
    }
    throw e
  }
}

export default instance
