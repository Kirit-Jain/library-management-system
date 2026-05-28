import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { IndianRupee } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogDescription, DialogFooter,
  DialogHeader, DialogTitle
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/labels'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { finesApi } from '@/api/fines'
import { formatCurrency, formatDate } from '@/lib/utils'
import type { Fine } from '@/types'

export function FinesPage() {
  const queryClient = useQueryClient()
  const [showUnpaidOnly, setShowUnpaidOnly] = useState(true)
  const [selectedFine, setSelectedFine] = useState<Fine | null>(null)
  const [paymentAmount, setPaymentAmount] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('CASH')

  const { data, isLoading } = useQuery({
    queryKey: ['fines', showUnpaidOnly],
    queryFn: () => showUnpaidOnly ? finesApi.getUnpaid() : finesApi.getAll(),
  })

  const payMutation = useMutation({
    mutationFn: finesApi.payFine,
    onSuccess: () => {
      toast.success('Payment recorded successfully!')
      queryClient.invalidateQueries({ queryKey: ['fines'] })
      setSelectedFine(null)
      setPaymentAmount('')
    },
  })

  const handlePay = () => {
    if (!selectedFine) return
    payMutation.mutate({
      fineId: selectedFine.id,
      amount: Number(paymentAmount),
      paymentMethod,
    })
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Fines</h1>
          <p className="text-muted-foreground">Manage fines and payments</p>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total Fines</p>
                <p className="text-2xl font-bold">{data?.totalElements || 0}</p>
              </div>
              <IndianRupee className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>{showUnpaidOnly ? 'Unpaid Fines' : 'All Fines'}</CardTitle>
            <Button
              variant="outline"
              size="sm"
              onClick={() => setShowUnpaidOnly(!showUnpaidOnly)}
            >
              Show {showUnpaidOnly ? 'All' : 'Unpaid Only'}
            </Button>
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
                  <TableHead>Type</TableHead>
                  <TableHead>Amount</TableHead>
                  <TableHead>Paid</TableHead>
                  <TableHead>Remaining</TableHead>
                  <TableHead>Date</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data?.content?.map((fine) => (
                  <TableRow key={fine.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium text-sm">{fine.memberName}</p>
                        <p className="text-xs font-mono text-muted-foreground">
                          {fine.membershipNumber}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className="text-sm max-w-xs truncate">
                      {fine.bookTitle}
                    </TableCell>
                    <TableCell>
                      <Badge variant="outline" className="text-xs">
                        {fine.fineType}
                      </Badge>
                    </TableCell>
                    <TableCell className="font-medium">
                      {formatCurrency(fine.amount)}
                    </TableCell>
                    <TableCell className="text-green-600">
                      {formatCurrency(fine.paidAmount)}
                    </TableCell>
                    <TableCell className="text-orange-600 font-medium">
                      {formatCurrency(fine.remainingAmount)}
                    </TableCell>
                    <TableCell className="text-sm">{formatDate(fine.fineDate)}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          fine.status === 'PAID'
                            ? 'success'
                            : fine.status === 'WAIVED'
                            ? 'secondary'
                            : 'destructive'
                        }
                      >
                        {fine.status}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {fine.status === 'UNPAID' && (
                        <Button
                          size="sm"
                          onClick={() => {
                            setSelectedFine(fine)
                            setPaymentAmount(fine.remainingAmount.toString())
                          }}
                        >
                          Pay
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      {/* Payment Dialog */}
      <Dialog open={!!selectedFine} onOpenChange={(open) => !open && setSelectedFine(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Record Payment</DialogTitle>
            <DialogDescription>
              Member: {selectedFine?.memberName}
              <br />
              Remaining: {formatCurrency(selectedFine?.remainingAmount || 0)}
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Amount</Label>
              <Input
                type="number"
                value={paymentAmount}
                onChange={(e) => setPaymentAmount(e.target.value)}
                max={selectedFine?.remainingAmount}
              />
            </div>
            <div className="space-y-2">
              <Label>Payment Method</Label>
              <Select value={paymentMethod} onValueChange={setPaymentMethod}>
                <SelectTrigger>
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="CASH">Cash</SelectItem>
                  <SelectItem value="CARD">Card</SelectItem>
                  <SelectItem value="ONLINE">Online</SelectItem>
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setSelectedFine(null)}>
              Cancel
            </Button>
            <Button onClick={handlePay} disabled={payMutation.isPending}>
              {payMutation.isPending ? 'Processing...' : 'Record Payment'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}