import { useQuery } from '@tanstack/react-query'
import {
  BookOpen,
  Users,
  ArrowRightLeft,
  AlertCircle,
  TrendingUp,
  IndianRupee,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { dashboardApi } from '@/api/dashboard'
import { formatCurrency } from '@/lib/utils'

export function DashboardPage() {
  const { data: stats, isLoading } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: dashboardApi.getStats,
  })

  if (isLoading) {
    return <div>Loading...</div>
  }

  const statCards = [
    {
      title: 'Total Books',
      value: stats?.totalBooks || 0,
      subtitle: `${stats?.totalBookCopies || 0} physical copies`,
      icon: BookOpen,
      color: 'text-blue-600',
      bg: 'bg-blue-100',
    },
    {
      title: 'Total Members',
      value: stats?.totalMembers || 0,
      subtitle: `${stats?.activeMembers || 0} active`,
      icon: Users,
      color: 'text-green-600',
      bg: 'bg-green-100',
    },
    {
      title: 'Active Borrowings',
      value: stats?.activeBorrowings || 0,
      subtitle: `${stats?.borrowingsToday || 0} today`,
      icon: ArrowRightLeft,
      color: 'text-purple-600',
      bg: 'bg-purple-100',
    },
    {
      title: 'Overdue Books',
      value: stats?.overdueBorrowings || 0,
      subtitle: 'Need attention',
      icon: AlertCircle,
      color: 'text-red-600',
      bg: 'bg-red-100',
    },
    {
      title: 'Unpaid Fines',
      value: formatCurrency(stats?.totalUnpaidFines || 0),
      subtitle: 'Pending collection',
      icon: IndianRupee,
      color: 'text-orange-600',
      bg: 'bg-orange-100',
    },
    {
      title: 'Available Copies',
      value: stats?.availableCopies || 0,
      subtitle: `${stats?.borrowedCopies || 0} borrowed`,
      icon: TrendingUp,
      color: 'text-teal-600',
      bg: 'bg-teal-100',
    },
  ]

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">Dashboard</h1>
        <p className="text-muted-foreground">Welcome to your library overview</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {statCards.map((stat) => (
          <Card key={stat.title} className="hover:shadow-lg transition-shadow">
            <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
              <CardTitle className="text-sm font-medium text-muted-foreground">
                {stat.title}
              </CardTitle>
              <div className={`p-2 rounded-lg ${stat.bg}`}>
                <stat.icon className={`w-4 h-4 ${stat.color}`} />
              </div>
            </CardHeader>
            <CardContent>
              <div className="text-2xl font-bold">{stat.value}</div>
              <p className="text-xs text-muted-foreground mt-1">{stat.subtitle}</p>
            </CardContent>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <Card>
          <CardHeader>
            <CardTitle>Today's Activity</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                <span className="text-sm font-medium">Borrowings Today</span>
                <span className="text-2xl font-bold text-blue-600">
                  {stats?.borrowingsToday || 0}
                </span>
              </div>
              <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
                <span className="text-sm font-medium">Returns Today</span>
                <span className="text-2xl font-bold text-green-600">
                  {stats?.returnsToday || 0}
                </span>
              </div>
              <div className="flex justify-between items-center p-3 bg-purple-50 rounded-lg">
                <span className="text-sm font-medium">Pending Reservations</span>
                <span className="text-2xl font-bold text-purple-600">
                  {stats?.pendingReservations || 0}
                </span>
              </div>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Financial Overview</CardTitle>
          </CardHeader>
          <CardContent>
            <div className="space-y-3">
              <div className="p-3 bg-red-50 rounded-lg">
                <p className="text-sm text-muted-foreground">Outstanding Fines</p>
                <p className="text-2xl font-bold text-red-600">
                  {formatCurrency(stats?.totalUnpaidFines || 0)}
                </p>
              </div>
              <div className="p-3 bg-green-50 rounded-lg">
                <p className="text-sm text-muted-foreground">Total Collected</p>
                <p className="text-2xl font-bold text-green-600">
                  {formatCurrency(stats?.totalCollectedFines || 0)}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  )
}