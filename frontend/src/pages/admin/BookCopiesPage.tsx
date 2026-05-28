import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import {
  BookMarked,
  Plus,
  Trash2,
  Search,
  MapPin,
  Package,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { Label } from '@/components/ui/labels'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import { Skeleton } from '@/components/ui/skeleton'
import {
  Dialog,
  DialogContent,
  DialogDescription,
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
import { bookCopiesApi } from '@/api/bookCopies'
import { shelvesApi, branchesApi } from '@/api/shelves'

export function BookCopiesPage() {
  const queryClient = useQueryClient()
  const [search, setSearch] = useState('')
  const [createOpen, setCreateOpen] = useState(false)
  const [selectedBookId, setSelectedBookId] = useState<number | null>(null)
  const [copiesByBook, setCopiesByBook] = useState<Record<number, any[]>>({})
  const [loadingBookId, setLoadingBookId] = useState<number | null>(null)

  const [formData, setFormData] = useState({
    bookId: '',
    barcode: '',
    shelfId: '',
    branchId: '',
    condition: 'GOOD' as 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'DAMAGED',
    acquisitionDate: new Date().toISOString().split('T')[0],
    price: '',
  })

  // Fetch all books
  const { data: booksData, isLoading: booksLoading } = useQuery({
    queryKey: ['books-for-copies', search],
    queryFn: () => (search ? booksApi.search(search) : booksApi.getAll(0, 50)),
  })

  // Fetch shelves and branches when dialog opens
  const { data: shelves } = useQuery({
    queryKey: ['shelves'],
    queryFn: shelvesApi.getAll,
    enabled: createOpen,
  })

  const { data: branches } = useQuery({
    queryKey: ['branches'],
    queryFn: branchesApi.getAll,
    enabled: createOpen,
  })

  // Create copy mutation
  const createMutation = useMutation({
    mutationFn: bookCopiesApi.create,
    onSuccess: (newCopy) => {
      toast.success(`Copy added! Barcode: ${newCopy.barcode}`)
      queryClient.invalidateQueries({ queryKey: ['books-for-copies'] })

      // Refresh copies for the book if visible
      if (selectedBookId === Number(formData.bookId)) {
        loadCopiesForBook(Number(formData.bookId))
      }

      setCreateOpen(false)
      resetForm()
    },
    onError: (error: any) => {
      const response = error.response?.data
      if (response?.validationErrors) {
        const errors = Object.values(response.validationErrors).join(', ')
        toast.error(errors)
      } else {
        toast.error(response?.message || 'Failed to create copy')
      }
    },
  })

  // Delete copy mutation
  const deleteMutation = useMutation({
    mutationFn: bookCopiesApi.delete,
    onSuccess: () => {
      toast.success('Copy deleted')
      queryClient.invalidateQueries({ queryKey: ['books-for-copies'] })
      if (selectedBookId) {
        loadCopiesForBook(selectedBookId)
      }
    },
    onError: (error: any) => {
      toast.error(error.response?.data?.message || 'Failed to delete copy')
    },
  })

  const loadCopiesForBook = async (bookId: number) => {
    try {
      setLoadingBookId(bookId)
      const copies = await bookCopiesApi.getByBook(bookId)
      setCopiesByBook((prev) => ({ ...prev, [bookId]: copies }))
    } catch (error) {
      toast.error('Failed to load copies')
    } finally {
      setLoadingBookId(null)
    }
  }

  const handleToggleBook = (bookId: number) => {
    if (selectedBookId === bookId) {
      setSelectedBookId(null)
    } else {
      setSelectedBookId(bookId)
      if (!copiesByBook[bookId]) {
        loadCopiesForBook(bookId)
      }
    }
  }

  const resetForm = () => {
    setFormData({
      bookId: '',
      barcode: '',
      shelfId: '',
      branchId: '',
      condition: 'GOOD',
      acquisitionDate: new Date().toISOString().split('T')[0],
      price: '',
    })
  }

  const handleCreate = () => {
    if (!formData.bookId) {
      toast.error('Please select a book')
      return
    }

    const payload: any = {
      bookId: Number(formData.bookId),
      condition: formData.condition,
    }

    if (formData.barcode) payload.barcode = formData.barcode
    if (formData.shelfId) payload.shelfId = Number(formData.shelfId)
    if (formData.branchId) payload.branchId = Number(formData.branchId)
    if (formData.acquisitionDate) payload.acquisitionDate = formData.acquisitionDate
    if (formData.price) payload.price = Number(formData.price)

    createMutation.mutate(payload)
  }

  const handleQuickAdd = (bookId: number) => {
    setFormData({ ...formData, bookId: bookId.toString() })
    setCreateOpen(true)
  }

  const getStatusBadge = (status: string) => {
    const variants: Record<string, any> = {
      AVAILABLE: 'success',
      BORROWED: 'default',
      RESERVED: 'warning',
      LOST: 'destructive',
      MAINTENANCE: 'secondary',
    }
    return <Badge variant={variants[status] || 'default'}>{status}</Badge>
  }

  const getConditionBadge = (condition: string) => {
    const colors: Record<string, string> = {
      EXCELLENT: 'bg-green-100 text-green-800',
      GOOD: 'bg-blue-100 text-blue-800',
      FAIR: 'bg-yellow-100 text-yellow-800',
      POOR: 'bg-orange-100 text-orange-800',
      DAMAGED: 'bg-red-100 text-red-800',
    }
    return (
      <span className={`px-2 py-1 rounded text-xs font-medium ${colors[condition] || ''}`}>
        {condition}
      </span>
    )
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Book Copies</h1>
          <p className="text-muted-foreground">
            Manage physical copies of books
          </p>
        </div>

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
              Add Copy
            </Button>
          </DialogTrigger>

          <DialogContent className="max-w-md">
            <DialogHeader>
              <DialogTitle>Add Book Copy</DialogTitle>
              <DialogDescription>
                Add a new physical copy of a book to the library
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {/* Book Selection */}
              <div className="space-y-2">
                <Label>
                  Book <span className="text-red-500">*</span>
                </Label>
                <Select
                  value={formData.bookId}
                  onValueChange={(value) =>
                    setFormData({ ...formData, bookId: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select a book..." />
                  </SelectTrigger>
                  <SelectContent>
                    {!booksData?.content?.length ? (
                      <div className="px-2 py-3 text-sm text-muted-foreground text-center">
                        No books available
                      </div>
                    ) : (
                      booksData.content.map((book) => (
                        <SelectItem key={book.id} value={book.id.toString()}>
                          {book.title} ({book.isbn})
                        </SelectItem>
                      ))
                    )}
                  </SelectContent>
                </Select>
              </div>

              {/* Barcode (optional - auto-generated) */}
              <div className="space-y-2">
                <Label>Barcode</Label>
                <Input
                  value={formData.barcode}
                  onChange={(e) =>
                    setFormData({ ...formData, barcode: e.target.value })
                  }
                  placeholder="Leave empty for auto-generation"
                />
                <p className="text-xs text-muted-foreground">
                  💡 Leave empty to auto-generate a unique barcode
                </p>
              </div>

              {/* Branch */}
              <div className="space-y-2">
                <Label>Branch</Label>
                <Select
                  value={formData.branchId}
                  onValueChange={(value) =>
                    setFormData({ ...formData, branchId: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select branch (optional)" />
                  </SelectTrigger>
                  <SelectContent>
                    {branches?.map((branch) => (
                      <SelectItem key={branch.id} value={branch.id.toString()}>
                        {branch.name}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Shelf */}
              <div className="space-y-2">
                <Label>Shelf Location</Label>
                <Select
                  value={formData.shelfId}
                  onValueChange={(value) =>
                    setFormData({ ...formData, shelfId: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Select shelf (optional)" />
                  </SelectTrigger>
                  <SelectContent>
                    {shelves?.map((shelf) => (
                      <SelectItem key={shelf.id} value={shelf.id.toString()}>
                        {shelf.shelfCode}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>

              {/* Condition */}
              <div className="space-y-2">
                <Label>Condition</Label>
                <Select
                  value={formData.condition}
                  onValueChange={(value: any) =>
                    setFormData({ ...formData, condition: value })
                  }
                >
                  <SelectTrigger>
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="EXCELLENT">Excellent</SelectItem>
                    <SelectItem value="GOOD">Good</SelectItem>
                    <SelectItem value="FAIR">Fair</SelectItem>
                    <SelectItem value="POOR">Poor</SelectItem>
                    <SelectItem value="DAMAGED">Damaged</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              {/* Acquisition Date & Price */}
              <div className="grid grid-cols-2 gap-3">
                <div className="space-y-2">
                  <Label>Acquisition Date</Label>
                  <Input
                    type="date"
                    value={formData.acquisitionDate}
                    onChange={(e) =>
                      setFormData({ ...formData, acquisitionDate: e.target.value })
                    }
                  />
                </div>
                <div className="space-y-2">
                  <Label>Price (₹)</Label>
                  <Input
                    type="number"
                    value={formData.price}
                    onChange={(e) =>
                      setFormData({ ...formData, price: e.target.value })
                    }
                    placeholder="350"
                    min="0"
                    step="0.01"
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
                disabled={createMutation.isPending}
              >
                {createMutation.isPending ? 'Adding...' : 'Add Copy'}
              </Button>
            </DialogFooter>
          </DialogContent>
        </Dialog>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total Books</p>
                <p className="text-2xl font-bold">
                  {booksData?.totalElements || 0}
                </p>
              </div>
              <BookMarked className="w-8 h-8 text-blue-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Total Copies</p>
                <p className="text-2xl font-bold">
                  {booksData?.content?.reduce(
                    (sum: number, b: any) => sum + b.totalCopies,
                    0
                  ) || 0}
                </p>
              </div>
              <Package className="w-8 h-8 text-green-500" />
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardContent className="p-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-muted-foreground">Available</p>
                <p className="text-2xl font-bold text-green-600">
                  {booksData?.content?.reduce(
                    (sum: number, b: any) => sum + b.availableCopies,
                    0
                  ) || 0}
                </p>
              </div>
              <MapPin className="w-8 h-8 text-purple-500" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Books List with Copies */}
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between">
            <CardTitle>Books & Their Copies</CardTitle>
            <div className="relative w-64">
              <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
              <Input
                placeholder="Search books..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="pl-9"
              />
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {booksLoading ? (
            <div className="space-y-2">
              {[...Array(5)].map((_, i) => (
                <Skeleton key={i} className="h-16" />
              ))}
            </div>
          ) : !booksData?.content?.length ? (
            <div className="text-center py-12">
              <BookMarked className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
              <p className="text-muted-foreground">No books in catalog yet</p>
              <p className="text-sm text-muted-foreground mt-2">
                Go to Books page to add books first
              </p>
            </div>
          ) : (
            <div className="space-y-2">
              {booksData.content.map((book) => (
                <div
                  key={book.id}
                  className="border rounded-lg overflow-hidden"
                >
                  {/* Book Header */}
                  <div
                    className="p-4 bg-muted/30 hover:bg-muted/50 cursor-pointer flex items-center justify-between"
                    onClick={() => handleToggleBook(book.id)}
                  >
                    <div className="flex-1">
                      <div className="flex items-center gap-3">
                        <BookMarked className="w-5 h-5 text-blue-500" />
                        <div>
                          <h3 className="font-semibold">{book.title}</h3>
                          <p className="text-sm text-muted-foreground font-mono">
                            ISBN: {book.isbn}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p className="text-xs text-muted-foreground">Copies</p>
                        <p className="font-semibold">
                          {book.availableCopies} / {book.totalCopies}
                        </p>
                      </div>

                      <Button
                        size="sm"
                        variant="outline"
                        onClick={(e) => {
                          e.stopPropagation()
                          handleQuickAdd(book.id)
                        }}
                      >
                        <Plus className="w-3 h-3 mr-1" />
                        Add Copy
                      </Button>

                      <span className="text-muted-foreground">
                        {selectedBookId === book.id ? '▼' : '▶'}
                      </span>
                    </div>
                  </div>

                  {/* Expanded Copies List */}
                  {selectedBookId === book.id && (
                    <div className="border-t">
                      {loadingBookId === book.id ? (
                        <div className="p-4 space-y-2">
                          <Skeleton className="h-8" />
                          <Skeleton className="h-8" />
                        </div>
                      ) : !copiesByBook[book.id]?.length ? (
                        <div className="p-8 text-center">
                          <Package className="w-8 h-8 mx-auto text-muted-foreground mb-2" />
                          <p className="text-sm text-muted-foreground">
                            No copies yet. Click "Add Copy" to create one.
                          </p>
                        </div>
                      ) : (
                        <Table>
                          <TableHeader>
                            <TableRow>
                              <TableHead>Barcode</TableHead>
                              <TableHead>Location</TableHead>
                              <TableHead>Status</TableHead>
                              <TableHead>Condition</TableHead>
                              <TableHead>Actions</TableHead>
                            </TableRow>
                          </TableHeader>
                          <TableBody>
                            {copiesByBook[book.id].map((copy: any) => (
                              <TableRow key={copy.id}>
                                <TableCell className="font-mono text-xs">
                                  {copy.barcode}
                                </TableCell>
                                <TableCell className="text-sm">
                                  {copy.shelf?.shelfCode ? (
                                    <div className="flex items-center gap-1">
                                      <MapPin className="w-3 h-3" />
                                      {copy.shelf.shelfCode}
                                    </div>
                                  ) : (
                                    <span className="text-muted-foreground">-</span>
                                  )}
                                </TableCell>
                                <TableCell>
                                  {getStatusBadge(copy.status)}
                                </TableCell>
                                <TableCell>
                                  {getConditionBadge(copy.condition)}
                                </TableCell>
                                <TableCell>
                                  <Button
                                    size="sm"
                                    variant="ghost"
                                    onClick={() => {
                                      if (copy.status === 'BORROWED') {
                                        toast.error(
                                          'Cannot delete - book is borrowed'
                                        )
                                        return
                                      }
                                      if (
                                        confirm(
                                          'Delete this copy? This cannot be undone.'
                                        )
                                      ) {
                                        deleteMutation.mutate(copy.id)
                                      }
                                    }}
                                  >
                                    <Trash2 className="w-4 h-4 text-red-500" />
                                  </Button>
                                </TableCell>
                              </TableRow>
                            ))}
                          </TableBody>
                        </Table>
                      )}
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}