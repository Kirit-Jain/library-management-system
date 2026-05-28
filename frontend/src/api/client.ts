import axios from 'axios'
import { toast } from 'sonner'

const api = axios.create({
  baseURL: '/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor - add token to every request
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// Response interceptor - handle errors globally
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { response } = error

    if (response) {
      switch (response.status) {
        case 401:
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('user')
          window.location.href = '/login'
          toast.error('Session expired. Please login again.')
          break
        case 403:
          toast.error('You do not have permission for this action.')
          break
        case 429:
          toast.error('Too many requests. Please wait a moment.')
          break
        case 500:
          toast.error('Server error. Please try again.')
          break
        default:
          { const message = response.data?.message || 'Something went wrong'
          toast.error(message) }
      }
    } else {
      toast.error('Network error. Check your connection.')
    }

    return Promise.reject(error)
  }
)

export default api