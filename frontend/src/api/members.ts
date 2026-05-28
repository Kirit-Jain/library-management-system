import api from './client'
import type { Member, MemberCreateRequest, MemberBorrowingHistory, ApiResponse, PageResponse } from '@/types'

export const membersApi = {
  getAll: async (page = 0, size = 10): Promise<PageResponse<Member>> => {
    const response = await api.get<ApiResponse<PageResponse<Member>>>(
      `/members?page=${page}&size=${size}`
    )
    return response.data.data
  },

  getById: async (id: number): Promise<Member> => {
    const response = await api.get<ApiResponse<Member>>(`/members/${id}`)
    return response.data.data
  },

  getHistory: async (id: number): Promise<MemberBorrowingHistory> => {
    const response = await api.get<ApiResponse<MemberBorrowingHistory>>(`/members/${id}/history`)
    return response.data.data
  },

  create: async (data: MemberCreateRequest): Promise<Member> => {
    const response = await api.post<ApiResponse<Member>>('/members', data)
    return response.data.data
  },

  getMyProfile: async (): Promise<Member> => {
    const response = await api.get<ApiResponse<Member>>('/members/my-profile')
    return response.data.data
  },
}