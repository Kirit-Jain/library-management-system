import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { toast } from 'sonner'
import { FileText, FileSpreadsheet, Calendar } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/labels'
import { reportsApi } from '@/api/reports'
import { downloadBlob, formatCurrency } from '@/lib/utils'
import type { MostBorrowedBook, ActiveMember, OverdueReport } from '@/types'

export function ReportsPage() {
  const [startDate, setStartDate] = useState(
    new Date(new Date().getFullYear(), 0, 1).toISOString().split('T')[0]
  )
  const [endDate, setEndDate] = useState(new Date().toISOString().split('T')[0])

  const { data: mostBorrowed } = useQuery({
    queryKey: ['report-most-borrowed', startDate, endDate],
    queryFn: () => reportsApi.mostBorrowed(startDate, endDate, 10),
  })

  const { data: overdue } = useQuery<OverdueReport>({
    queryKey: ['report-overdue'],
    queryFn: () => reportsApi.overdue(),
  })

  const { data: activeMembers } = useQuery({
    queryKey: ['report-active-members', startDate, endDate],
    queryFn: () => reportsApi.activeMembers(startDate, endDate, 10),
  })

  const { data: inventory } = useQuery({
    queryKey: ['report-inventory'],
    queryFn: () => reportsApi.inventory(),
  })

  const downloadReport = async (type: 'excel' | 'pdf', reportName: string) => {
    try {
      const params: Record<string, string> = {}
      if (reportName !== 'overdue' && reportName !== 'inventory') {
        params.startDate = startDate
        params.endDate = endDate
        params.limit = '20'
      }

      const blob = type === 'excel'
        ? await reportsApi.downloadExcel(reportName, params)
        : await reportsApi.downloadPdf(reportName, params)

      const ext = type === 'excel' ? 'xlsx' : 'pdf'
      downloadBlob(blob, `${reportName}-report.${ext}`)
      toast.success(`Report downloaded!`)
    } catch {
      toast.error('Failed to download report')
    }
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Reports</h1>
          <p className="text-muted-foreground">Generate and download reports</p>
        </div>
      </div>

      {/* Date Range Filter */}
      <Card>
        <CardContent className="p-4">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label>Start Date</Label>
              <div className="relative">
                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  type="date"
                  value={startDate}
                  onChange={(e) => setStartDate(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>
            <div className="space-y-2">
              <Label>End Date</Label>
              <div className="relative">
                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
                <Input
                  type="date"
                  value={endDate}
                  onChange={(e) => setEndDate(e.target.value)}
                  className="pl-9"
                />
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Reports Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {/* Most Borrowed */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Most Borrowed Books</CardTitle>
                <CardDescription>Top 10 popular books</CardDescription>
              </div>
              <FileText className="w-5 h-5 text-blue-500" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 mb-4">
              {mostBorrowed?.slice(0, 5).map((book: MostBorrowedBook) => (
                <div key={book.bookId} className="flex justify-between text-sm">
                  <span className="truncate flex-1">{book.rank}. {book.title}</span>
                  <span className="font-medium text-muted-foreground ml-2">
                    {book.borrowCount}
                  </span>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('excel', 'most-borrowed')}
              >
                <FileSpreadsheet className="w-4 h-4 mr-1" />
                Excel
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('pdf', 'most-borrowed')}
              >
                <FileText className="w-4 h-4 mr-1" />
                PDF
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Overdue Report */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Overdue Books</CardTitle>
                <CardDescription>Books past their due date</CardDescription>
              </div>
              <FileText className="w-5 h-5 text-red-500" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 mb-4">
              <div className="flex justify-between text-sm">
                <span>Total Overdue</span>
                <span className="font-bold text-red-600">
                  {overdue?.totalOverdueBorrowings || 0}
                </span>
              </div>
              <div className="flex justify-between text-sm">
                <span>Total Fines</span>
                <span className="font-bold text-orange-600">
                  {formatCurrency(overdue?.totalOverdueFines || 0)}
                </span>
              </div>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('excel', 'overdue')}
              >
                <FileSpreadsheet className="w-4 h-4 mr-1" />
                Excel
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('pdf', 'overdue')}
              >
                <FileText className="w-4 h-4 mr-1" />
                PDF
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Active Members */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Active Members</CardTitle>
                <CardDescription>Top borrowers in period</CardDescription>
              </div>
              <FileText className="w-5 h-5 text-green-500" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="space-y-2 mb-4">
              {activeMembers?.slice(0, 5).map((member: ActiveMember) => (
                <div key={member.memberId} className="flex justify-between text-sm">
                  <span className="truncate flex-1">{member.rank}. {member.fullName}</span>
                  <span className="font-medium text-muted-foreground ml-2">
                    {member.borrowCount}
                  </span>
                </div>
              ))}
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('excel', 'active-members')}
              >
                <FileSpreadsheet className="w-4 h-4 mr-1" />
                Excel
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('pdf', 'active-members')}
              >
                <FileText className="w-4 h-4 mr-1" />
                PDF
              </Button>
            </div>
          </CardContent>
        </Card>

        {/* Inventory */}
        <Card>
          <CardHeader>
            <div className="flex items-center justify-between">
              <div>
                <CardTitle className="text-lg">Inventory Report</CardTitle>
                <CardDescription>Current stock status</CardDescription>
              </div>
              <FileText className="w-5 h-5 text-purple-500" />
            </div>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 gap-2 mb-4 text-sm">
              <div>
                <p className="text-muted-foreground">Total Books</p>
                <p className="text-xl font-bold">{inventory?.totalBooks || 0}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Total Copies</p>
                <p className="text-xl font-bold">{inventory?.totalCopies || 0}</p>
              </div>
              <div>
                <p className="text-muted-foreground">Available</p>
                <p className="text-xl font-bold text-green-600">
                  {inventory?.availableCopies || 0}
                </p>
              </div>
              <div>
                <p className="text-muted-foreground">Borrowed</p>
                <p className="text-xl font-bold text-orange-600">
                  {inventory?.borrowedCopies || 0}
                </p>
              </div>
            </div>
            <div className="flex gap-2">
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('excel', 'inventory')}
              >
                <FileSpreadsheet className="w-4 h-4 mr-1" />
                Excel
              </Button>
              <Button
                variant="outline"
                size="sm"
                className="flex-1"
                onClick={() => downloadReport('pdf', 'inventory')}
              >
                <FileText className="w-4 h-4 mr-1" />
                PDF
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
