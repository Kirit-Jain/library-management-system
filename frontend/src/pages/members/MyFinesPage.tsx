import { useQuery } from '@tanstack/react-query'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { finesApi } from '@/api/fines'
import { formatCurrency, formatDate } from '@/lib/utils'

export function MyFinesPage() {
  const { data: fines } = useQuery({
    queryKey: ['my-fines'],
    queryFn: finesApi.getMy,
  })

  const totalUnpaid = fines?.filter(f => f.status === 'UNPAID')
    .reduce((sum, f) => sum + f.remainingAmount, 0) || 0

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">My Fines</h1>
        <p className="text-muted-foreground">Your fine history</p>
      </div>

      {totalUnpaid > 0 && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="p-6">
            <p className="text-sm text-red-700">Total Unpaid Amount</p>
            <p className="text-3xl font-bold text-red-700">{formatCurrency(totalUnpaid)}</p>
            <p className="text-sm text-red-600 mt-2">
              Please visit the library to pay your fines.
            </p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>All Fines</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Book</TableHead>
                <TableHead>Type</TableHead>
                <TableHead>Amount</TableHead>
                <TableHead>Paid</TableHead>
                <TableHead>Date</TableHead>
                <TableHead>Status</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {fines?.map((fine) => (
                <TableRow key={fine.id}>
                  <TableCell className="font-medium">{fine.bookTitle}</TableCell>
                  <TableCell>
                    <Badge variant="outline">{fine.fineType}</Badge>
                  </TableCell>
                  <TableCell className="font-medium">{formatCurrency(fine.amount)}</TableCell>
                  <TableCell className="text-green-600">{formatCurrency(fine.paidAmount)}</TableCell>
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
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}