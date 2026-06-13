import axios from 'axios'

const request = axios.create({
  baseURL: '',
  timeout: 10000
})

request.interceptors.response.use(
  (response) => {
    const body = response.data

    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body.data
      }

      const error = new Error(body.message || '请求处理失败')
      error.code = body.code
      throw error
    }

    return body
  },
  (error) => {
    const message =
      error.response?.data?.message ||
      error.response?.data?.error ||
      error.message ||
      '网络请求失败'

    throw new Error(message)
  }
)

export default request
