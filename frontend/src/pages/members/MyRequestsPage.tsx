import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { borrowRequestsApi } from '@/api/borrowRequests'
import { membersApi } from '@/api/members'
import { formatDateTime } from '@/lib/utils'

export function MyRequestsPage() {
  const { data: profile } = useQuery({
    queryKey: ['my-profile'],
    queryFn: membersApi.getMyProfile,
  })

  const { data: requests } = useQuery({
    queryKey: ['my-requests', profile?.id],
    queryFn: () => borrowRequestsApi.getMyRequests(profile!.id),
    enabled: !!profile?.id,
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">My Borrow Requests</h1>
        <p className="text-muted-foreground">Track your borrow requests</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Requests</CardTitle>
        </CardHeader>
        <CardContent>
          {!requests?.length ? (
            <p className="text-center text-muted-foreground py-8">
              No requests yet. Browse books and request to borrow!
            </p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Book</TableHead>
                  <TableHead>Requested</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Reason</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {requests.map((req) => (
                  <TableRow key={req.id}>
                    <TableCell className="font-medium">{req.bookTitle}</TableCell>
                    <TableCell className="text-sm">{formatDateTime(req.createdAt)}</TableCell>
                    <TableCell>
                      <Badge
                        variant={
                          req.status === 'APPROVED'
                            ? 'success'
                            : req.status === 'REJECTED'
                            ? 'destructive'
                            : 'default'
                        }
                      >
                        {req.status}
                      </Badge>
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {req.rejectionReason || '-'}
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