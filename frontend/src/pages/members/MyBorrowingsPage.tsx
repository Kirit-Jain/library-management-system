import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { borrowingsApi } from '@/api/borrowings'
import { formatDateTime } from '@/lib/utils'

export function MyBorrowingsPage() {
  const { data: borrowings } = useQuery({
    queryKey: ['my-borrowings'],
    queryFn: borrowingsApi.getMy,
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">My Borrowings</h1>
        <p className="text-muted-foreground">Your borrowing history</p>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Borrowings</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Book</TableHead>
                <TableHead>Borrowed</TableHead>
                <TableHead>Due Date</TableHead>
                <TableHead>Returned</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {borrowings?.map((b) => (
                <TableRow key={b.id}>
                  <TableCell className="font-medium">{b.bookTitle}</TableCell>
                  <TableCell className="text-sm">{formatDateTime(b.borrowDate)}</TableCell>
                  <TableCell className="text-sm">
                    {formatDateTime(b.dueDate)}
                    {b.isOverdue && b.status === 'ACTIVE' && (
                      <p className="text-xs text-red-500">{b.overdueDays} days late</p>
                    )}
                  </TableCell>
                  <TableCell className="text-sm">
                    {b.returnDate ? formatDateTime(b.returnDate) : '-'}
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
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}