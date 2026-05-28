import api from './client'
import type { ApiResponse, PageResponse, User, LibrarySetting } from '@/types'


export const adminApi = {
  getUsers: async (page = 0, size = 20): Promise<PageResponse<User>> => {
    const response = await api.get<ApiResponse<PageResponse<User>>>(
      `/admin/users?page=${page}&size=${size}`
    )
    return response.data.data
  },

  lockUser: async (id: number): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(`/admin/users/${id}/lock`)
    return response.data.data
  },

  unlockUser: async (id: number): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(`/admin/users/${id}/unlock`)
    return response.data.data
  },

  deactivateUser: async (id: number): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(`/admin/users/${id}/deactivate`)
    return response.data.data
  },

  activateUser: async (id: number): Promise<User> => {
    const response = await api.put<ApiResponse<User>>(`/admin/users/${id}/activate`)
    return response.data.data
  },

  getSettings: async (): Promise<LibrarySetting[]> => {
    const response = await api.get<ApiResponse<LibrarySetting[]>>('/admin/settings')
    return response.data.data
  },

  updateSetting: async (key: string, value: string): Promise<LibrarySetting> => {
    const response = await api.put<ApiResponse<LibrarySetting>>(
      `/admin/settings/${key}`,
      { value }
    )
    return response.data.data
  },
}