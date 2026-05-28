import { useParams, Link } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ArrowLeft, Mail, Phone, Calendar, BookOpen, AlertCircle, IndianRupee } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Skeleton } from '@/components/ui/skeleton'
import { membersApi } from '@/api/members'
import { formatCurrency, formatDate, formatDateTime } from '@/lib/utils'
import type { Borrowing } from '@/types'

export function MemberDetailPage() {
  const { id } = useParams()

  const { data: member, isLoading } = useQuery({
    queryKey: ['member', id],
    queryFn: () => membersApi.getById(Number(id)),
  })

  const { data: history } = useQuery({
    queryKey: ['member-history', id],
    queryFn: () => membersApi.getHistory(Number(id)),
  })

  if (isLoading) {
    return (
      <div className="space-y-4">
        <Skeleton className="h-12 w-64" />
        <Skeleton className="h-32" />
        <Skeleton className="h-64" />
      </div>
    )
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex items-center gap-4">
        <Link to="/members">
          <Button variant="ghost" size="icon">
            <ArrowLeft className="w-4 h-4" />
          </Button>
        </Link>
        <div>
          <h1 className="text-3xl font-bold">{member?.fullName}</h1>
          <p className="text-muted-foreground font-mono">{member?.membershipNumber}</p>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Borrowed</p>
                <p className="text-2xl font-bold">{member?.currentBorrowedCount}</p>
              </div>
              <BookOpen className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total History</p>
                <p className="text-2xl font-bold">{history?.totalBorrowed || 0}</p>
              </div>
              <Calendar className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Overdue</p>
                <p className="text-2xl font-bold text-red-500">
                  {history?.totalOverdue || 0}
                </p>
              </div>
              <AlertCircle className="w-8 h-8 text-red-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Pending Fines</p>
                <p className="text-2xl font-bold text-orange-500">
                  {formatCurrency(member?.totalFinesPending || 0)}
                </p>
              </div>
              <IndianRupee className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Member Info */}
      <Card>
        <CardHeader>
          <CardTitle>Member Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center gap-2 text-sm">
              <Mail className="w-4 h-4 text-muted-foreground" />
              <span>{member?.email}</span>
            </div>
            {member?.phone && (
              <div className="flex items-center gap-2 text-sm">
                <Phone className="w-4 h-4 text-muted-foreground" />
                <span>{member.phone}</span>
              </div>
            )}
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="w-4 h-4 text-muted-foreground" />
              <span>Member since: {formatDate(member?.membershipStart || '')}</span>
            </div>
            <div className="flex items-center gap-2 text-sm">
              <Calendar className="w-4 h-4 text-muted-foreground" />
              <span>Expires: {formatDate(member?.membershipExpiry || '')}</span>
            </div>
          </div>
          <div className="flex gap-2 pt-2">
            <Badge variant="outline">{member?.membershipType}</Badge>
            <Badge variant={member?.isActive ? 'success' : 'destructive'}>
              {member?.isActive ? 'Active' : 'Inactive'}
            </Badge>
          </div>
        </CardContent>
      </Card>

      {/* Active Borrowings */}
      {history?.activeBorrowings && history.activeBorrowings.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle>Currently Borrowed Books</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              {history.activeBorrowings.map((b: Borrowing) => (
                <div key={b.id} className="flex items-center justify-between p-3 border rounded-lg">
                  <div>
                    <p className="font-medium">{b.bookTitle}</p>
                    <p className="text-sm text-muted-foreground">
                      Due: {formatDateTime(b.dueDate)}
                    </p>
                  </div>
                  {b.isOverdue && (
                    <Badge variant="destructive">
                      {b.overdueDays} days overdue
                    </Badge>
                  )}
                </div>
              ))}
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
