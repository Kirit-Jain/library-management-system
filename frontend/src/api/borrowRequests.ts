import api from './client'
import type { ApiResponse, PageResponse } from '@/types'

export interface BorrowRequest {
  id: number
  memberId: number
  memberName: string
  membershipNumber: string
  memberEmail: string
  bookId: number
  bookTitle: string
  bookIsbn: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'CANCELLED'
  approvedBy?: number
  approvedAt?: string
  rejectionReason?: string
  createdAt: string
}

export const borrowRequestsApi = {
  create: async (memberId: number, bookId: number): Promise<BorrowRequest> => {
    const response = await api.post<ApiResponse<BorrowRequest>>('/borrow-requests', {
      memberId,
      bookId,
    })
    return response.data.data
  },

  getPending: async (): Promise<PageResponse<BorrowRequest>> => {
    const response = await api.get<ApiResponse<PageResponse<BorrowRequest>>>(
      '/borrow-requests/pending'
    )
    return response.data.data
  },

  getMyRequests: async (memberId: number): Promise<BorrowRequest[]> => {
    const response = await api.get<ApiResponse<BorrowRequest[]>>(
      `/borrow-requests/member/${memberId}`
    )
    return response.data.data
  },

  approve: async (id: number): Promise<BorrowRequest> => {
    const response = await api.post<ApiResponse<BorrowRequest>>(
      `/borrow-requests/${id}/approve`
    )
    return response.data.data
  },

  reject: async (id: number, reason: string): Promise<BorrowRequest> => {
    const response = await api.post<ApiResponse<BorrowRequest>>(
      `/borrow-requests/${id}/reject?reason=${encodeURIComponent(reason)}`
    )
    return response.data.data
  },
}