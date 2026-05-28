import api from './client'
import type { BookCopy, BookCopyCreateRequest, ApiResponse } from '@/types'

export const bookCopiesApi = {
  getByBook: async (bookId: number): Promise<BookCopy[]> => {
    const response = await api.get<ApiResponse<BookCopy[]>>(
      `/book-copies/book/${bookId}`
    )
    return response.data.data
  },

  getByBarcode: async (barcode: string): Promise<BookCopy> => {
    const response = await api.get<ApiResponse<BookCopy>>(
      `/book-copies/barcode/${barcode}`
    )
    return response.data.data
  },

  create: async (data: BookCopyCreateRequest): Promise<BookCopy> => {
    const response = await api.post<ApiResponse<BookCopy>>('/book-copies', data)
    return response.data.data
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/book-copies/${id}`)
  },
}