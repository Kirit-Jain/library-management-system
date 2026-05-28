import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
// import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/labels'
import { borrowRequestsApi } from '@/api/borrowRequests'
import { formatDateTime } from '@/lib/utils'

export function BorrowRequestsPage() {
  const queryClient = useQueryClient()
  const [rejectingId, setRejectingId] = useState<number | null>(null)
  const [rejectReason, setRejectReason] = useState('')

  const { data } = useQuery({
    queryKey: ['borrow-requests-pending'],
    queryFn: borrowRequestsApi.getPending,
  })

  const approveMutation = useMutation({
    mutationFn: borrowRequestsApi.approve,
    onSuccess: () => {
      toast.success('Request approved!')
      queryClient.invalidateQueries({ queryKey: ['borrow-requests-pending'] })
    },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, reason }: { id: number; reason: string }) =>
      borrowRequestsApi.reject(id, reason),
    onSuccess: () => {
      toast.success('Request rejected')
      queryClient.invalidateQueries({ queryKey: ['borrow-requests-pending'] })
      setRejectingId(null)
      setRejectReason('')
    },
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">Borrow Requests</h1>
        <p className="text-muted-foreground">Approve or reject member borrow requests</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Pending Requests ({data?.totalElements || 0})</CardTitle>
        </CardHeader>
        <CardContent>
          {!data?.content?.length ? (
            <p className="text-center text-muted-foreground py-8">
              No pending requests
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Member</TableHead>
                  <TableHead>Book</TableHead>
                  <TableHead>Requested</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {data.content.map((req) => (
                  <TableRow key={req.id}>
                    <TableCell>
                      <div>
                        <p className="font-medium">{req.memberName}</p>
                        <p className="text-xs font-mono text-muted-foreground">
                          {req.membershipNumber}
                        </p>
                      </div>
                    </TableCell>
                    <TableCell className="font-medium">{req.bookTitle}</TableCell>
                    <TableCell className="text-sm">{formatDateTime(req.createdAt)}</TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        <Button
                          size="sm"
                          onClick={() => approveMutation.mutate(req.id)}
                          disabled={approveMutation.isPending}
                        >
                          Approve
                        </Button>
                        <Button
                          size="sm"
                          variant="destructive"
                          onClick={() => setRejectingId(req.id)}
                        >
                          Reject
                        </Button>
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Dialog open={!!rejectingId} onOpenChange={(open) => !open && setRejectingId(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Reject Request</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Reason for rejection</Label>
              <Input
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
                placeholder="Enter reason..."
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setRejectingId(null)}>
              Cancel
            </Button>
            <Button
              variant="destructive"
              onClick={() => rejectMutation.mutate({ id: rejectingId!, reason: rejectReason })}
              disabled={!rejectReason || rejectMutation.isPending}
            >
              Reject Request
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}