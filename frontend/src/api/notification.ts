import api from './client'
import type { Notification, ApiResponse, PageResponse } from '@/types'

export const notificationsApi = {
  getUserNotifications: async (userId: number, page = 0, size = 20): Promise<PageResponse<Notification>> => {
    const response = await api.get<ApiResponse<PageResponse<Notification>>>(
      `/notifications/user/${userId}?page=${page}&size=${size}`
    )
    return response.data.data
  },

  getUnreadCount: async (userId: number): Promise<number> => {
    const response = await api.get<ApiResponse<{ unreadCount: number }>>(
      `/notifications/user/${userId}/unread-count`
    )
    return response.data.data.unreadCount
  },

  markAsRead: async (id: number): Promise<void> => {
    await api.put(`/notifications/${id}/read`)
  },

  markAllAsRead: async (userId: number): Promise<void> => {
    await api.put(`/notifications/user/${userId}/read-all`)
  },
}