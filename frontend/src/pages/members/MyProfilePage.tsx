import { useQuery } from '@tanstack/react-query'
import { Mail, Phone, Calendar, BookOpen, IndianRupee, CreditCard } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Skeleton } from '@/components/ui/skeleton'
import { membersApi } from '@/api/members'
import { formatCurrency, formatDate } from '@/lib/utils'

export function MyProfilePage() {
  const { data: member, isLoading } = useQuery({
    queryKey: ['my-profile'],
    queryFn: membersApi.getMyProfile,
  })

  if (isLoading) {
    return <Skeleton className="h-96" />
  }

  if (!member) {
    return (
      <Card>
        <CardContent className="p-8 text-center">
          <p className="text-muted-foreground">
            No membership found. Please contact a librarian to create your membership.
          </p>
        </CardContent>
      </Card>
    )
  }

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">My Profile</h1>
        <p className="text-muted-foreground">Your membership details</p>
      </div>

      {/* Membership Card */}
      <Card className="bg-gradient-to-br from-blue-600 to-indigo-700 text-white border-0 shadow-xl">
        <CardContent className="p-8">
          <div className="flex justify-between items-start mb-6">
            <div>
              <p className="text-blue-100 text-sm mb-1">MEMBERSHIP CARD</p>
              <h2 className="text-3xl font-bold">{member.fullName}</h2>
            </div>
            <CreditCard className="w-12 h-12 opacity-50" />
          </div>

          <div className="space-y-2 mb-6">
            <p className="text-xs text-blue-100">MEMBERSHIP NUMBER</p>
            <p className="text-2xl font-mono tracking-wider">{member.membershipNumber}</p>
          </div>

          <div className="grid grid-cols-2 gap-4 pt-4 border-t border-white/20">
            <div>
              <p className="text-xs text-blue-100">TYPE</p>
              <p className="font-semibold">{member.membershipType}</p>
            </div>
            <div>
              <p className="text-xs text-blue-100">VALID UNTIL</p>
              <p className="font-semibold">{formatDate(member.membershipExpiry)}</p>
            </div>
          </div>
        </CardContent>
      </Card>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Currently Borrowed</p>
                <p className="text-2xl font-bold">{member.currentBorrowedCount}</p>
                <p className="text-xs text-muted-foreground">of {member.maxBooksAllowed} max</p>
              </div>
              <BookOpen className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Borrow Period</p>
                <p className="text-2xl font-bold">{member.maxBorrowDays}</p>
                <p className="text-xs text-muted-foreground">days per book</p>
              </div>
              <Calendar className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Pending Fines</p>
                <p className={`text-2xl font-bold ${
                  member.totalFinesPending > 0 ? 'text-red-500' : 'text-green-500'
                }`}>
                  {formatCurrency(member.totalFinesPending)}
                </p>
              </div>
              <IndianRupee className="w-8 h-8 text-orange-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Personal Info */}
      <Card>
        <CardHeader>
          <CardTitle>Personal Information</CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex items-center gap-3">
              <Mail className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Email</p>
                <p className="font-medium">{member.email}</p>
              </div>
            </div>
            {member.phone && (
              <div className="flex items-center gap-3">
                <Phone className="w-5 h-5 text-muted-foreground" />
                <div>
                  <p className="text-xs text-muted-foreground">Phone</p>
                  <p className="font-medium">{member.phone}</p>
                </div>
              </div>
            )}
            <div className="flex items-center gap-3">
              <Calendar className="w-5 h-5 text-muted-foreground" />
              <div>
                <p className="text-xs text-muted-foreground">Member Since</p>
                <p className="font-medium">{formatDate(member.membershipStart)}</p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}