import api from './client'
import type { Fine, ApiResponse, PageResponse } from '@/types'

export const finesApi = {
  getAll: async (page = 0, size = 10): Promise<PageResponse<Fine>> => {
    const response = await api.get<ApiResponse<PageResponse<Fine>>>(
      `/fines?page=${page}&size=${size}`
    )
    return response.data.data
  },

  getUnpaid: async (page = 0, size = 10): Promise<PageResponse<Fine>> => {
    const response = await api.get<ApiResponse<PageResponse<Fine>>>(
      `/fines/unpaid?page=${page}&size=${size}`
    )
    return response.data.data
  },

  getMemberFines: async (memberId: number): Promise<Fine[]> => {
    const response = await api.get<ApiResponse<Fine[]>>(`/fines/member/${memberId}`)
    return response.data.data
  },

  payFine: async (data: {
    fineId: number
    amount: number
    paymentMethod: string
    transactionId?: string
  }): Promise<Fine> => {
    const response = await api.post<ApiResponse<Fine>>('/fines/pay', data)
    return response.data.data
  },

  waiveFine: async (id: number, reason: string): Promise<Fine> => {
    const response = await api.post<ApiResponse<Fine>>(
      `/fines/${id}/waive?reason=${encodeURIComponent(reason)}`
    )
    return response.data.data
  },

  getMy: async (): Promise<Fine[]> => {
    const response = await api.get<ApiResponse<Fine[]>>('/fines/my')
    return response.data.data
  },
}