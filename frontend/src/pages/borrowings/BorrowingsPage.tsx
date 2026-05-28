import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { ArrowRightLeft, RotateCcw, CheckCircle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogDescription, DialogFooter,
  DialogHeader, DialogTitle, DialogTrigger
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/labels'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { borrowingsApi } from '@/api/borrowings'
import { formatDateTime } from '@/lib/utils'

export function BorrowingsPage() {
  const queryClient = useQueryClient()
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [borrowOpen, setBorrowOpen] = useState(false)
  const [memberId, setMemberId] = useState('')
  const [barcode, setBarcode] = useState('')

  const { data, isLoading } = useQuery({
    queryKey: ['borrowings', statusFilter],
    queryFn: () =>
      borrowingsApi.getAll(0, 50, statusFilter === 'all' ? undefined : statusFilter),
  })

  const borrowMutation = useMutation({
    mutationFn: () => borrowingsApi.borrow(Number(memberId), barcode),
    onSuccess: () => {
      toast.success('Book borrowed successfully!')
      queryClient.invalidateQueries({ queryKey: ['borrowings'] })
      setBorrowOpen(false)
      setMemberId('')
      setBarcode('')
    },
  })

  const returnMutation = useMutation({
    mutationFn: ({ id, condition }: { id: number; condition: string }) =>
      borrowingsApi.return(id, condition),
    onSuccess: () => {
      toast.success('Book returned successfully!')
      queryClient.invalidateQueries({ queryKey: ['borrowings'] })
    },
  })

  const renewMutation = useMutation({
    mutationFn: borrowingsApi.renew,
    onSuccess: () => {
      toast.success('Book renewed successfully!')
      queryClient.invalidateQueries({ queryKey: ['borrowings'] })
    },
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Borrowings</h1>
          <p className="text-muted-foreground">Manage book borrowings</p>
        </div>

        <Dialog open={borrowOpen} onOpenChange={setBorrowOpen}>
          <DialogTrigger asChild>
            <Button>
              <ArrowRightLeft className="w-4 h-4 mr-2" />
              Borrow Book
            </Button>
          </DialogTrigger>
          <DialogContent>
            <DialogHeader>
              <DialogTitle>Borrow a Book</DialogTitle>
              <DialogDescription>
                Enter member ID and book barcode to issue a book.
              </DialogDescription>
            </DialogHeader>
            <div className="space-y-4 py-4">
              <div className="space-y-2">
                <Label>Member ID</Label>
                <Input
                  placeholder="Enter member ID"
                  value={memberId}
                  onChange={(e) => setMemberId(e.target.value)}
                />
              </div>
              <div className="space-y-2">
                <Label>Book Barcode</Label>
                <Input
                  placeholder="Scan or enter barcode"
                  value={barcode}
                  onChange={(e) => setBarcode(e.target.value)}
                />
              </div>
            </div>
            <DialogFooter>
              <Button variant="outline" onClick={() => setBorrowOpen(false)}>
                Cancel
              </Button>
              <Button
                onClick={() => borrowMutation.mutate()}
                disabled={!memberId || !barcode || borrowMutation.isPending}
              >
                {borrowMutation.isPending ? 'Processing...' : 'Borrow'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Borrowings</CardTitle>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-48">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Status</SelectItem>
                <SelectItem value="ACTIVE">Active</SelectItem>
                <SelectItem value="RETURNED">Returned</SelectItem>
                <SelectItem value="OVERDUE">Overdue</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-12" />
              ))}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Member</TableHead>
                  <TableHead>Book</TableHead>
                  <TableHead>Barcode</TableHead>
                  <TableHead>Borrowed</TableHead>
                  <TableHead>Due Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.content?.map((b) => (
                  <TableRow key={b.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{b.memberName}</p>
                        <p className="text-xs text-muted-foreground font-mono">
                          {b.membershipNumber}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className="max-w-xs truncate">{b.bookTitle}</TableCell>
                    <TableCell className="font-mono text-xs">{b.barcode}</TableCell>
                    <TableCell className="text-sm">{formatDateTime(b.borrowDate)}</TableCell>
                    <TableCell className="text-sm">
                      <div>
                        {formatDateTime(b.dueDate)}
                        {b.isOverdue && (
                          <p className="text-xs text-red-500">
                            {b.overdueDays} days late
                          </p>
                        )}
                      </div>
                    </TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          b.status === 'ACTIVE'
                            ? 'default'
                            : b.status === 'OVERDUE'
                            ? 'destructive'
                            : b.status === 'RETURNED'
                            ? 'success'
                            : 'secondary'
                        }
                      >
                        {b.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {b.status === 'ACTIVE' || b.status === 'OVERDUE' ? (
                        <div className="flex gap-1">
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => renewMutation.mutate(b.id)}
                            disabled={renewMutation.isPending}
                          >
                            <RotateCcw className="w-3 h-3 mr-1" />
                            Renew
                          </Button>
                          <Button
                            size="sm"
                            onClick={() => returnMutation.mutate({ id: b.id, condition: 'GOOD' })}
                            disabled={returnMutation.isPending}
                          >
                            <CheckCircle className="w-3 h-3 mr-1" />
                            Return
                          </Button>
                        </div>
                      ) : (
                        <span className="text-xs text-muted-foreground">-</span>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}