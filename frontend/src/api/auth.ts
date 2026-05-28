import api from './client'
import type { LoginRequest, RegisterRequest, JwtResponse, ApiResponse } from '@/types'

export const authApi = {
  login: async (data: LoginRequest): Promise<JwtResponse> => {
    const response = await api.post<ApiResponse<JwtResponse>>('/auth/login', data)
    return response.data.data
  },

  register: async (data: RegisterRequest): Promise<JwtResponse> => {
    const response = await api.post<ApiResponse<JwtResponse>>('/auth/register', data)
    return response.data.data
  },

  refreshToken: async (refreshToken: string): Promise<JwtResponse> => {
    const response = await api.post<ApiResponse<JwtResponse>>(
      `/auth/refresh-token?refreshToken=${refreshToken}`
    )
    return response.data.data
  },
}