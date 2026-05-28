import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Search, BookOpen, Plus, BookMarked, AlertCircle } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Label } from '@/components/ui/labels'
import { Textarea } from '@/components/ui/textarea'
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { booksApi } from '@/api/books'
import { borrowRequestsApi } from '@/api/borrowRequests'
import { membersApi } from '@/api/members'
import { publishersApi } from '@/api/publisher'
import { useAuthStore } from '@/store/auth'

export function BooksPage() {
  const queryClient = useQueryClient()
  const { user } = useAuthStore()
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)

  const [formData, setFormData] = useState({
    isbn: '',
    title: '',
    description: '',
    publisherId: '',
    publicationYear: '',
    edition: '',
    language: 'English',
    pageCount: '',
    coverImageUrl: '',
  })

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'LIBRARIAN'

  // Fetch books
  const { data, isLoading } = useQuery({
    queryKey: ['books', search],
    queryFn: () => (search ? booksApi.search(search) : booksApi.getAll(0, 20)),
  })

  // Fetch member profile (only for non-admins)
  const {
    data: profile,
    isLoading: profileLoading,
    error: profileError,
  } = useQuery({
    queryKey: ['my-profile'],
    queryFn: membersApi.getMyProfile,
    enabled: !isAdmin,
    retry: false,
  })

  // Fetch publishers (only when dialog opens)
  const { data: publishers } = useQuery({
    queryKey: ['publishers'],
    queryFn: publishersApi.getAll,
    enabled: createOpen && isAdmin,
  })

  // Create book mutation
  const createMutation = useMutation({
    mutationFn: booksApi.create,
    onSuccess: () => {
      toast.success('Book created successfully!')
      queryClient.invalidateQueries({ queryKey: ['books'] })
      setCreateOpen(false)
      resetForm()
    },
    onError: (error: any) => {
      const response = error.response?.data
      if (response?.validationErrors) {
        const errors = Object.values(response.validationErrors).join(', ')
        toast.error(errors)
      } else {
        toast.error(response?.message || 'Failed to create book')
      }
    },
  })

  // Borrow request mutation (for members)
  const requestMutation = useMutation({
    mutationFn: ({ bookId }: { bookId: number }) => {
      if (!profile?.id) {
        throw new Error('No membership found')
      }
      return borrowRequestsApi.create(profile.id, bookId)
    },
    onSuccess: () => {
      toast.success('Borrow request submitted! Wait for librarian approval.')
      queryClient.invalidateQueries({ queryKey: ['my-requests'] })
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to submit request')
    },
  })

  const resetForm = () => {
    setFormData({
      isbn: '',
      title: '',
      description: '',
      publisherId: '',
      publicationYear: '',
      edition: '',
      language: 'English',
      pageCount: '',
      coverImageUrl: '',
    })
  }

  const handleCreate = () => {
    if (!formData.isbn || !formData.title) {
      toast.error('ISBN and Title are required')
      return
    }

    createMutation.mutate({
      isbn: formData.isbn,
      title: formData.title,
      description: formData.description || undefined,
      publisherId: formData.publisherId ? Number(formData.publisherId) : undefined,
      publicationYear: formData.publicationYear
        ? Number(formData.publicationYear)
        : undefined,
      edition: formData.edition || undefined,
      language: formData.language,
      pageCount: formData.pageCount ? Number(formData.pageCount) : undefined,
      coverImageUrl: formData.coverImageUrl || undefined,
      authorIds: [],
      categoryIds: [],
    })
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Books Catalog</h1>
          <p className="text-muted-foreground">
            {isAdmin ? 'Manage library books' : 'Browse and request books'}
          </p>
        </div>

        {/* Add Book button - Admin/Librarian only */}
        {isAdmin && (
          <Dialog
            open={createOpen}
            onOpenChange={(open) => {
              setCreateOpen(open)
              if (!open) resetForm()
            }}
          >
            <DialogTrigger asChild>
              <Button size="lg">
                <Plus className="w-4 h-4 mr-2" />
                Add Book
              </Button>
            </DialogTrigger>

            <DialogContent className="max-w-lg max-h-[90vh] overflow-y-auto">
              <DialogHeader>
                <DialogTitle>Add New Book</DialogTitle>
              </DialogHeader>

              <div className="space-y-3 py-4">
                <div className="space-y-2">
                  <Label>
                    ISBN <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    value={formData.isbn}
                    onChange={(e) =>
                      setFormData({ ...formData, isbn: e.target.value })
                    }
                    placeholder="9780061122415"
                    maxLength={13}
                  />
                </div>

                <div className="space-y-2">
                  <Label>
                    Title <span className="text-red-500">*</span>
                  </Label>
                  <Input
                    value={formData.title}
                    onChange={(e) =>
                      setFormData({ ...formData, title: e.target.value })
                    }
                    placeholder="Book title"
                  />
                </div>

                <div className="space-y-2">
                  <Label>Publisher</Label>
                  <Select
                    value={formData.publisherId}
                    onValueChange={(value) =>
                      setFormData({ ...formData, publisherId: value })
                    }
                  >
                    <SelectTrigger>
                      <SelectValue placeholder="Select publisher (optional)" />
                    </SelectTrigger>
                    <SelectContent>
                      {!publishers || publishers.length === 0 ? (
                        <div className="px-2 py-3 text-sm text-muted-foreground text-center">
                          No publishers found
                        </div>
                      ) : (
                        publishers.map((pub) => (
                          <SelectItem key={pub.id} value={pub.id.toString()}>
                            {pub.name}
                          </SelectItem>
                        ))
                      )}
                    </SelectContent>
                  </Select>
                </div>

                <div className="space-y-2">
                  <Label>Description</Label>
                  <Textarea
                    value={formData.description}
                    onChange={(e) =>
                      setFormData({ ...formData, description: e.target.value })
                    }
                    placeholder="Brief description"
                    rows={3}
                  />
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label>Year</Label>
                    <Input
                      type="number"
                      value={formData.publicationYear}
                      onChange={(e) =>
                        setFormData({
                          ...formData,
                          publicationYear: e.target.value,
                        })
                      }
                      placeholder="2024"
                      min="1000"
                      max="2100"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Pages</Label>
                    <Input
                      type="number"
                      value={formData.pageCount}
                      onChange={(e) =>
                        setFormData({ ...formData, pageCount: e.target.value })
                      }
                      placeholder="208"
                      min="1"
                    />
                  </div>
                </div>

                <div className="grid grid-cols-2 gap-3">
                  <div className="space-y-2">
                    <Label>Edition</Label>
                    <Input
                      value={formData.edition}
                      onChange={(e) =>
                        setFormData({ ...formData, edition: e.target.value })
                      }
                      placeholder="1st"
                    />
                  </div>
                  <div className="space-y-2">
                    <Label>Language</Label>
                    <Select
                      value={formData.language}
                      onValueChange={(value) =>
                        setFormData({ ...formData, language: value })
                      }
                    >
                      <SelectTrigger>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="English">English</SelectItem>
                        <SelectItem value="Hindi">Hindi</SelectItem>
                        <SelectItem value="Spanish">Spanish</SelectItem>
                        <SelectItem value="French">French</SelectItem>
                        <SelectItem value="German">German</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
              </div>

              <DialogFooter>
                <Button variant="outline" onClick={() => setCreateOpen(false)}>
                  Cancel
                </Button>
                <Button
                  onClick={handleCreate}
                  disabled={createMutation.isPending}
                >
                  {createMutation.isPending ? 'Creating...' : 'Create Book'}
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {/* Member: Warning if no membership */}
      {!isAdmin && !profileLoading && (profileError || !profile) && (
        <Card className="border-yellow-200 bg-yellow-50">
          <CardContent className="p-4">
            <div className="flex items-start gap-3">
              <AlertCircle className="w-5 h-5 text-yellow-600 mt-0.5" />
              <div className="flex-1">
                <p className="font-medium text-yellow-900">
                  No Membership Found
                </p>
                <p className="text-sm text-yellow-800 mt-1">
                  You need an active membership to borrow books. Please contact
                  the librarian to create your membership.
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Member: Membership Info */}
      {!isAdmin && profile && (
        <Card className="bg-blue-50 border-blue-200">
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-blue-800">
                  Logged in as{' '}
                  <span className="font-semibold">{profile.fullName}</span>
                </p>
                <p className="text-xs text-blue-700 font-mono">
                  Member ID: {profile.membershipNumber}
                </p>
              </div>
              <div className="text-right">
                <p className="text-xs text-blue-700">Borrowing Limit</p>
                <p className="font-semibold text-blue-900">
                  {profile.currentBorrowedCount} / {profile.maxBooksAllowed}
                </p>
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Search */}
      <div className="relative max-w-md">
        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
        <Input
          placeholder="Search by title, author, ISBN..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          className="pl-9"
        />
      </div>

      {/* Books Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {[...Array(8)].map((_, i) => (
            <Card key={i} className="animate-pulse">
              <CardContent className="p-4">
                <div className="aspect-[3/4] bg-muted rounded-lg mb-3" />
                <div className="h-4 bg-muted rounded mb-2" />
                <div className="h-3 bg-muted rounded w-2/3" />
              </CardContent>
            </Card>
          ))}
        </div>
      ) : !data?.content || data.content.length === 0 ? (
        <Card>
          <CardContent className="p-12 text-center">
            <BookOpen className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
            <p className="text-muted-foreground mb-2">
              {search ? 'No books match your search' : 'No books in the catalog'}
            </p>
            {!search && isAdmin && (
              <p className="text-sm text-muted-foreground">
                Click "Add Book" to add the first book
              </p>
            )}
          </CardContent>
        </Card>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
          {data.content.map((book) => (
            <Card
              key={book.id}
              className="hover:shadow-lg transition-shadow flex flex-col"
            >
              <CardContent className="p-4 flex flex-col flex-1">
                {/* Book Cover */}
                <div className="aspect-[3/4] bg-gradient-to-br from-blue-100 to-indigo-100 rounded-lg mb-3 flex items-center justify-center overflow-hidden">
                  {book.coverImageUrl ? (
                    <img
                      src={book.coverImageUrl}
                      alt={book.title}
                      className="w-full h-full object-cover"
                      onError={(e) => {
                        e.currentTarget.style.display = 'none'
                      }}
                    />
                  ) : (
                    <BookOpen className="w-16 h-16 text-blue-400" />
                  )}
                </div>

                {/* Title */}
                <h3 className="font-semibold text-sm line-clamp-2 mb-1">
                  {book.title}
                </h3>

                {/* Authors */}
                <p className="text-xs text-muted-foreground mb-2 line-clamp-1">
                  {book.authors?.length > 0
                    ? book.authors.join(', ')
                    : 'Unknown Author'}
                </p>

                {/* ISBN */}
                <p className="text-xs text-muted-foreground font-mono mb-2">
                  ISBN: {book.isbn}
                </p>

                {/* Categories */}
                {book.categories && book.categories.length > 0 && (
                  <div className="flex flex-wrap gap-1 mb-2">
                    {book.categories.slice(0, 2).map((cat) => (
                      <Badge key={cat} variant="secondary" className="text-xs">
                        {cat}
                      </Badge>
                    ))}
                  </div>
                )}

                {/* Spacer to push button to bottom */}
                <div className="flex-1" />

                {/* Availability */}
                <div className="flex items-center justify-between mt-3 pt-3 border-t">
                  <div className="text-xs">
                    <p className="font-semibold">
                      {book.availableCopies}/{book.totalCopies}
                    </p>
                    <p className="text-muted-foreground">Available</p>
                  </div>
                  <Badge variant={book.available ? 'success' : 'destructive'}>
                    {book.available ? 'Available' : 'Unavailable'}
                  </Badge>
                </div>

                {/* ====================================== */}
                {/* MEMBER: REQUEST TO BORROW BUTTON     */}
                {/* ====================================== */}
                {!isAdmin && (
                  <div className="mt-3">
                    {profileLoading ? (
                      <Button size="sm" className="w-full" disabled>
                        Loading...
                      </Button>
                    ) : !profile ? (
                      <Button
                        size="sm"
                        className="w-full"
                        disabled
                        variant="outline"
                      >
                        No Membership
                      </Button>
                    ) : !book.available ? (
                      <Button
                        size="sm"
                        className="w-full"
                        disabled
                        variant="outline"
                      >
                        Not Available
                      </Button>
                    ) : (
                      <Button
                        size="sm"
                        className="w-full"
                        disabled={requestMutation.isPending}
                        onClick={() =>
                          requestMutation.mutate({ bookId: book.id })
                        }
                      >
                        <BookMarked className="w-3 h-3 mr-1" />
                        {requestMutation.isPending
                          ? 'Sending...'
                          : 'Request to Borrow'}
                      </Button>
                    )}
                  </div>
                )}

                {/* ====================================== */}
                {/* ADMIN: VIEW BUTTON                    */}
                {/* ====================================== */}
                {isAdmin && (
                  <Button
                    size="sm"
                    variant="outline"
                    className="w-full mt-3"
                    onClick={() => toast.info(`Book ID: ${book.id}`)}
                  >
                    View Details
                  </Button>
                )}
              </CardContent>
            </Card>
          ))}
        </div>
      )}
    </div>
  )
}