import api from './client'
import type { ApiResponse, Publisher } from '@/types'

export const publishersApi = {
  getAll: async (): Promise<Publisher[]> => {
    const response = await api.get<ApiResponse<Publisher[]>>('/publishers')
    return response.data.data
  },

  create: async (data: Partial<Publisher>): Promise<Publisher> => {
    const response = await api.post<ApiResponse<Publisher>>('/publishers', data)
    return response.data.data
  },
}