import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Link } from 'react-router-dom'
import { toast } from 'sonner'
import { Users, Search, UserPlus, Eye } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Label } from '@/components/ui/labels'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog, DialogContent, DialogDescription, DialogFooter,
  DialogHeader, DialogTitle, DialogTrigger
} from '@/components/ui/dialog'
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select'
import { membersApi } from '@/api/members'
import { adminApi } from '@/api/admin'
import { formatCurrency } from '@/lib/utils'
import { User } from '@/types'

export function MembersPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [formData, setFormData] = useState({
    userId: '',
    membershipType: 'STANDARD',
    membershipStartDate: new Date().toISOString().split('T')[0],
    membershipExpiryDate: new Date(new Date().setFullYear(new Date().getFullYear() + 1))
      .toISOString().split('T')[0],
  })

  // Fetch all members
  const { data, isLoading } = useQuery({
    queryKey: ['members'],
    queryFn: () => membersApi.getAll(0, 50),
  })

  // Fetch all users (to select from when creating member)
  const { data: usersData } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminApi.getUsers(0, 100),
    enabled: createOpen,
  })

  // Create member mutation
  const createMutation = useMutation({
    mutationFn: (data: any) => membersApi.create(data),
    onSuccess: () => {
      toast.success('Member created successfully!')
      queryClient.invalidateQueries({ queryKey: ['members'] })
      setCreateOpen(false)
      resetForm()
    },
    onError: (error: unknown) => {
      toast.error((error as { response?: { data?: { message?: string } } }).response?.data?.message || 'Failed to create member')
    },
  })

  const resetForm = () => {
    setFormData({
      userId: '',
      membershipType: 'STANDARD',
      membershipStartDate: new Date().toISOString().split('T')[0],
      membershipExpiryDate: new Date(new Date().setFullYear(new Date().getFullYear() + 1))
        .toISOString().split('T')[0],
    })
  }

  const handleCreate = () => {
    if (!formData.userId) {
      toast.error('Please select a user')
      return
    }

    createMutation.mutate({
      userId: Number(formData.userId),
      membershipType: formData.membershipType,
      membershipStartDate: formData.membershipStartDate,
      membershipExpiryDate: formData.membershipExpiryDate,
    })
  }

  // Filter users that don't already have membership
  const availableUsers = usersData?.content?.filter((user: User) => {
    const memberExists = data?.content?.some((m: any) => m.userId === user.id)
    return !memberExists
  }) || []

  const filteredMembers = data?.content?.filter(m =>
    m.fullName.toLowerCase().includes(search.toLowerCase()) ||
    m.membershipNumber.toLowerCase().includes(search.toLowerCase()) ||
    m.email.toLowerCase().includes(search.toLowerCase())
  ) || []

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header with Add Button */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Members</h1>
          <p className="text-muted-foreground">Manage library members</p>
        </div>

        <Dialog open={createOpen} onOpenChange={setCreateOpen}>
          <DialogTrigger asChild>
            <Button size="lg">
              <UserPlus className="w-4 h-4 mr-2" />
              Add Member
            </Button>
          </DialogTrigger>

          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Add New Member</DialogTitle>
              <DialogDescription>
                Create a library membership for an existing user
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {/* User Selection */}
              <div className="space-y-2">
                <Label>Select User *</Label>
                {availableUsers.length === 0 ? (
                  <div className="p-3 bg-yellow-50 border border-yellow-200 rounded-lg text-sm">
                    <p className="text-yellow-800">
                      No users available. All users already have memberships.
                    </p>
                    <p className="text-yellow-700 text-xs mt-1">
                      Users need to register first before getting a membership.
                    </p>
                  </div>
                ) : (
                  <Select
                    value={formData.userId}
                    onValueChange={(value) => setFormData({ ...formData, userId: value })}
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Choose a user..." />
                    </SelectTrigger>
                    <SelectContent>
                      {availableUsers.map((user: User) => (
                        <SelectItem key={user.id} value={user.id.toString()}>
                          {user.firstName} {user.lastName} ({user.email})
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                )}
                <p className="text-xs text-muted-foreground">
                  {availableUsers.length} user(s) available for membership
                </p>
              </div>

              {/* Membership Type */}
              <div className="space-y-2">
                <Label>Membership Type *</Label>
                <Select
                  value={formData.membershipType}
                  onValueChange={(value) => setFormData({ ...formData, membershipType: value })}
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="STANDARD">Standard (5 books, 14 days)</SelectItem>
                    <SelectItem value="PREMIUM">Premium (10 books, 21 days)</SelectItem>
                    <SelectItem value="STUDENT">Student (3 books, 14 days)</SelectItem>
                    <SelectItem value="SENIOR">Senior (7 books, 21 days)</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Dates */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>Start Date *</Label>
                  <Input
                    type="date"
                    value={formData.membershipStartDate}
                    onChange={(e) => setFormData({ ...formData, membershipStartDate: e.target.value })}
                  />
                </div>
                <div className="space-y-2">
                  <Label>Expiry Date *</Label>
                  <Input
                    type="date"
                    value={formData.membershipExpiryDate}
                    onChange={(e) => setFormData({ ...formData, membershipExpiryDate: e.target.value })}
                  />
                </div>
              </div>
            </div>

            <DialogFooter>
              <Button
                variant="outline"
                onClick={() => {
                  setCreateOpen(false)
                  resetForm()
                }}
              >
                Cancel
              </Button>
              <Button
                onClick={handleCreate}
                disabled={createMutation.isPending || availableUsers.length === 0}
              >
                {createMutation.isPending ? 'Creating...' : 'Create Member'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Stats Card */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total Members</p>
                <p className="text-2xl font-bold">{data?.totalElements || 0}</p>
              </div>
              <Users className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Members Table */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>All Members</CardTitle>
            <div className="relative w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search members..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="space-y-2">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-12" />
              ))}
            </div>
          ) : filteredMembers.length === 0 ? (
            <div className="text-center py-12">
              <Users className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
              <p className="text-muted-foreground mb-2">
                {search ? 'No members match your search' : 'No members yet'}
              </p>
              {!search && (
                <p className="text-sm text-muted-foreground">
                  Click "Add Member" above to create the first membership
                </p>
              )}
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Member #</TableHead>
                  <TableHead>Name</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Type</TableHead>
                  <TableHead>Borrowed</TableHead>
                  <TableHead>Fines</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredMembers.map((member) => (
                  <TableRow key={member.id}>
                    <TableCell className="font-mono text-sm">
                      {member.membershipNumber}
                    </TableCell>
                    <TableCell className="font-medium">{member.fullName}</TableCell>
                    <TableCell className="text-sm">{member.email}</TableCell>
                    <TableCell>
                      <Badge variant="outline">{member.membershipType}</Badge>
                    </TableCell>
                    <TableCell>
                      {member.currentBorrowedCount} / {member.maxBooksAllowed}
                    </TableCell>
                    <TableCell>
                      {member.totalFinesPending > 0 ? (
                        <span className="text-red-600 font-medium">
                          {formatCurrency(member.totalFinesPending)}
                        </span>
                      ) : (
                        <span className="text-muted-foreground">-</span>
                      )}
                    </TableCell>
                    <TableCell>
                      {member.membershipExpired ? (
                        <Badge variant="destructive">Expired</Badge>
                      ) : member.isActive ? (
                        <Badge variant="success">Active</Badge>
                      ) : (
                        <Badge variant="secondary">Inactive</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      <Link to={`/members/${member.id}`}>
                        <Button variant="ghost" size="sm">
                          <Eye className="w-4 h-4" />
                        </Button>
                      </Link>
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