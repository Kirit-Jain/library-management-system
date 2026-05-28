import api from './client'
import type {
  ApiResponse,
  MostBorrowedBook,
  ActiveMember,
  OverdueReport,
  FineCollectionReport,
  InventoryReport,
} from '@/types'


export const reportsApi = {
  mostBorrowed: async (startDate: string, endDate: string, limit = 10): Promise<MostBorrowedBook[]> => {
    const response = await api.get<ApiResponse<MostBorrowedBook[]>>(
      `/reports/most-borrowed?startDate=${startDate}&endDate=${endDate}&limit=${limit}`
    )
    return response.data.data
  },

  overdue: async (): Promise<OverdueReport> => {
    const response = await api.get<ApiResponse<OverdueReport>>('/reports/overdue')
    return response.data.data
  },

  activeMembers: async (startDate: string, endDate: string, limit = 10): Promise<ActiveMember[]> => {
    const response = await api.get<ApiResponse<ActiveMember[]>>(
      `/reports/active-members?startDate=${startDate}&endDate=${endDate}&limit=${limit}`
    )
    return response.data.data
  },

  fineCollection: async (startDate: string, endDate: string): Promise<FineCollectionReport> => {
    const response = await api.get<ApiResponse<FineCollectionReport>>(
      `/reports/fine-collection?startDate=${startDate}&endDate=${endDate}`
    )
    return response.data.data
  },

  inventory: async (): Promise<InventoryReport> => {
    const response = await api.get<ApiResponse<InventoryReport>>('/reports/inventory')
    return response.data.data
  },

  downloadExcel: async (reportType: string, params?: Record<string, string>): Promise<Blob> => {
    const queryString = params ? '?' + new URLSearchParams(params).toString() : ''
    const response = await api.get<ApiResponse<Blob>>(`/reports/${reportType}/export/excel${queryString}`, {
      responseType: 'blob',
    })
    return response.data.data
  },

  downloadPdf: async (reportType: string, params?: Record<string, string>): Promise<Blob> => {
    const queryString = params ? '?' + new URLSearchParams(params).toString() : ''
    const response = await api.get<ApiResponse<Blob>>(`/reports/${reportType}/export/pdf${queryString}`, {
      responseType: 'blob',
    })
    return response.data.data
  },
}