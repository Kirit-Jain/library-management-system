import api from './client'
import type { DashboardStats, ApiResponse } from '@/types'

export const dashboardApi = {
  getStats: async (): Promise<DashboardStats> => {
    const response = await api.get<ApiResponse<DashboardStats>>('/dashboard/stats')
    return response.data.data
  },
}