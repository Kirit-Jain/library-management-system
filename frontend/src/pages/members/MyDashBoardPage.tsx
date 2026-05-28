import { useQuery } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { BookOpen, IndianRupee, ClipboardList, AlertCircle } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { membersApi } from '@/api/members'
import { borrowingsApi } from '@/api/borrowings'
import { finesApi } from '@/api/fines'
import { formatCurrency, formatDate } from '@/lib/utils'

export function MyDashboardPage() {
  const { data: profile } = useQuery({
    queryKey: ['my-profile'],
    queryFn: membersApi.getMyProfile,
  })

  const { data: myBorrowings } = useQuery({
    queryKey: ['my-borrowings'],
    queryFn: borrowingsApi.getMy,
  })

  const { data: myFines } = useQuery({
    queryKey: ['my-fines'],
    queryFn: finesApi.getMy,
  })

  const activeBorrowings = myBorrowings?.filter(b => b.status === 'ACTIVE') || []
  const overdueBorrowings = myBorrowings?.filter(b => b.isOverdue) || []
  const unpaidFines = myFines?.filter(f => f.status === 'UNPAID') || []

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">Welcome back, {profile?.fullName?.split(' ')[0]}! 👋</h1>
        <p className="text-muted-foreground">
          Member ID: <span className="font-mono">{profile?.membershipNumber}</span>
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Active Borrowings</p>
                <p className="text-3xl font-bold">{activeBorrowings.length}</p>
              </div>
              <BookOpen className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Overdue</p>
                <p className="text-3xl font-bold text-red-500">{overdueBorrowings.length}</p>
              </div>
              <AlertCircle className="w-8 h-8 text-red-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Unpaid Fines</p>
                <p className="text-2xl font-bold text-orange-500">
                  {formatCurrency(profile?.totalFinesPending || 0)}
                </p>
              </div>
              <IndianRupee className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Books Limit</p>
                <p className="text-3xl font-bold">
                  {profile?.currentBorrowedCount}/{profile?.maxBooksAllowed}
                </p>
              </div>
              <ClipboardList className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <CardTitle>Currently Borrowed</CardTitle>
              <Link to="/my-borrowings">
                <Button variant="ghost" size="sm">View All</Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {activeBorrowings.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                No active borrowings
              </p>
            ) : (
              <div className="space-y-3">
                {activeBorrowings.slice(0, 5).map((b) => (
                  <div key={b.id} className="flex justify-between items-center p-3 border rounded-lg">
                    <div>
                      <p className="font-medium">{b.bookTitle}</p>
                      <p className="text-sm text-muted-foreground">
                        Due: {formatDate(b.dueDate)}
                      </p>
                    </div>
                    {b.isOverdue && (
                      <Badge variant="destructive">{b.overdueDays} days late</Badge>
                    )}
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <div className="flex justify-between items-center">
              <CardTitle>Recent Fines</CardTitle>
              <Link to="/my-fines">
                <Button variant="ghost" size="sm">View All</Button>
              </Link>
            </div>
          </CardHeader>
          <CardContent>
            {unpaidFines.length === 0 ? (
              <p className="text-center text-muted-foreground py-8">
                No pending fines 🎉
              </p>
            ) : (
              <div className="space-y-3">
                {unpaidFines.slice(0, 5).map((f) => (
                  <div key={f.id} className="flex justify-between items-center p-3 border rounded-lg">
                    <div>
                      <p className="font-medium text-sm">{f.bookTitle}</p>
                      <p className="text-xs text-muted-foreground">{f.fineType}</p>
                    </div>
                    <p className="font-bold text-red-500">{formatCurrency(f.amount)}</p>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}