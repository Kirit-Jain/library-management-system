import api from './client'
import type { Shelf, LibraryBranch, ApiResponse } from '@/types'

export const shelvesApi = {
  getAll: async (): Promise<Shelf[]> => {
    const response = await api.get<ApiResponse<Shelf[]>>('/shelves')
    return response.data.data
  },
}

export const branchesApi = {
  getAll: async (): Promise<LibraryBranch[]> => {
    const response = await api.get<ApiResponse<LibraryBranch[]>>('/branches')
    return response.data.data
  },
}