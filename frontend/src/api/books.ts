import api from './client'
import type { Book, BookLocation, BookCreateRequest, ApiResponse, PageResponse } from '@/types'

export const booksApi = {
  getAll: async (page = 0, size = 10): Promise<PageResponse<Book>> => {
    const response = await api.get<ApiResponse<PageResponse<Book>>>(
      `/books?page=${page}&size=${size}`
    )
    return response.data.data
  },

  getById: async (id: number): Promise<Book> => {
    const response = await api.get<ApiResponse<Book>>(`/books/${id}`)
    return response.data.data
  },

  search: async (query: string, page = 0, size = 10): Promise<PageResponse<Book>> => {
    const response = await api.get<ApiResponse<PageResponse<Book>>>(
      `/books/search?q=${query}&page=${page}&size=${size}`
    )
    return response.data.data
  },

  getLocation: async (id: number): Promise<BookLocation> => {
    const response = await api.get<ApiResponse<BookLocation>>(`/books/${id}/location`)
    return response.data.data
  },

  create: async (data: BookCreateRequest): Promise<Book> => {
    const response = await api.post<ApiResponse<Book>>('/books', data)
    return response.data.data
  },

  delete: async (id: number): Promise<void> => {
    await api.delete(`/books/${id}`)
  },
}