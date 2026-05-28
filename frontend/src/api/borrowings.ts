import api from './client'
import type { Borrowing, ApiResponse, PageResponse } from '@/types'

export const borrowingsApi = {
  getAll: async (page = 0, size = 10, status?: string): Promise<PageResponse<Borrowing>> => {
    const url = status
      ? `/borrowings?page=${page}&size=${size}&status=${status}`
      : `/borrowings?page=${page}&size=${size}`
    const response = await api.get<ApiResponse<PageResponse<Borrowing>>>(url)
    return response.data.data
  },

  getOverdue: async (): Promise<Borrowing[]> => {
    const response = await api.get<ApiResponse<Borrowing[]>>('/borrowings/overdue')
    return response.data.data
  },

  borrow: async (memberId: number, barcode: string): Promise<Borrowing> => {
    const response = await api.post<ApiResponse<Borrowing>>('/borrowings/borrow', {
      memberId,
      barcode,
    })
    return response.data.data
  },

  return: async (id: number, condition: string): Promise<Borrowing> => {
    const response = await api.post<ApiResponse<Borrowing>>(`/borrowings/${id}/return`, {
      borrowingId: id,
      condition,
    })
    return response.data.data
  },

  renew: async (id: number): Promise<Borrowing> => {
    const response = await api.post<ApiResponse<Borrowing>>(`/borrowings/${id}/renew`)
    return response.data.data
  },

  getMy: async (): Promise<Borrowing[]> => {
    const response = await api.get<ApiResponse<Borrowing[]>>('/borrowings/my')
    return response.data.data
  },
}