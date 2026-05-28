// ============================================
// API Response wrapper

import { ReactNode } from "react"

// ============================================
export interface ApiResponse<T> {
  success: boolean
  message?: string
  data: T
  error?: string
  validationErrors?: Record<string, string>
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number
  size: number
  first: boolean
  last: boolean
}

// ============================================
// User & Auth
// ============================================
export interface User {
  lastName: ReactNode
  firstName: ReactNode
  id: number
  username: string
  email: string
  fullName: string
  role: string
  isActive?: boolean
  isLocked?: boolean
}

export interface JwtResponse {
  accessToken: string
  refreshToken: string
  tokenType: string
  expiresIn: number
  user: User
}

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  username: string
  email: string
  password: string
  firstName: string
  lastName: string
  phone?: string
  address?: string
}

// ============================================
// Books
// ============================================
export interface Book {
  id: number
  isbn: string
  title: string
  description?: string
  publisherName?: string
  publicationYear?: number
  edition?: string
  language: string
  pageCount?: number
  coverImageUrl?: string
  totalCopies: number
  availableCopies: number
  available: boolean
  authors: string[]
  categories: string[]
  createdAt: string
}

export interface BookCreateRequest {
  isbn: string
  title: string
  description?: string
  publisherId?: number
  publicationYear?: number
  edition?: string
  language?: string
  pageCount?: number
  coverImageUrl?: string
  authorIds?: number[]
  categoryIds?: number[]
}

export interface BookLocation {
  bookId: number
  isbn: string
  title: string
  totalCopies: number
  availableCopies: number
  copies: BookCopyLocation[]
}

export interface BookCopyLocation {
  copyId: number
  barcode: string
  branchName?: string
  branchAddress?: string
  floorName?: string
  floorNumber?: number
  sectionName?: string
  sectionCode?: string
  shelfCode?: string
  status: string
  condition: string
  expectedReturnDate?: string
  locationDescription?: string
}

// ============================================
// Members
// ============================================
export interface Member {
  user: User
  id: number
  userId: number
  membershipNumber: string
  fullName: string
  email: string
  phone?: string
  membershipType: 'STANDARD' | 'PREMIUM' | 'STUDENT' | 'SENIOR'
  maxBooksAllowed: number
  maxBorrowDays: number
  membershipStart: string
  membershipExpiry: string
  isActive: boolean
  membershipExpired: boolean
  totalFinesPending: number
  currentBorrowedCount: number
}

export interface MemberCreateRequest {
  userId: number
  membershipType: 'STANDARD' | 'PREMIUM' | 'STUDENT' | 'SENIOR'
  membershipStartDate: string
  membershipExpiryDate: string
  maxBooksAllowed?: number
  maxBorrowDays?: number
}

export interface MemberBorrowingHistory {
  memberId: number
  memberName: string
  membershipNumber: string
  totalBorrowed: number
  currentlyBorrowed: number
  totalOverdue: number
  totalReturned: number
  totalFinesPaid: number
  totalFinesPending: number
  activeBorrowings: Borrowing[]
  borrowingHistory: Borrowing[]
  pendingFines: Fine[]
}

// ============================================
// Borrowings
// ============================================
export interface Borrowing {
  id: number
  memberId: number
  memberName: string
  membershipNumber: string
  bookCopyId: number
  barcode: string
  bookTitle: string
  bookIsbn: string
  borrowDate: string
  dueDate: string
  returnDate?: string
  renewedCount: number
  status: 'ACTIVE' | 'RETURNED' | 'OVERDUE' | 'LOST'
  isOverdue: boolean
  overdueDays: number
  notes?: string
  createdAt: string
}

// ============================================
// Fines
// ============================================
export interface Fine {
  id: number
  memberId: number
  memberName: string
  membershipNumber: string
  borrowingId: number
  bookTitle: string
  barcode: string
  amount: number
  paidAmount: number
  remainingAmount: number
  fineType: 'OVERDUE' | 'LOST_BOOK' | 'DAMAGED_BOOK'
  status: 'UNPAID' | 'PAID' | 'WAIVED'
  fineDate: string
  dueDate?: string
  paidDate?: string
  notes?: string
}

// ============================================
// Dashboard
// ============================================
export interface DashboardStats {
  totalBooks: number
  totalBookCopies: number
  availableCopies: number
  borrowedCopies: number
  totalMembers: number
  activeMembers: number
  activeBorrowings: number
  overdueBorrowings: number
  borrowingsToday: number
  returnsToday: number
  totalUnpaidFines: number
  totalCollectedFines: number
  pendingReservations: number
}

// ============================================
// Notifications
// ============================================
export interface Notification {
  id: number
  title: string
  message: string
  type: string
  isRead: boolean
  createdAt: string
}

// ============================================
// Admin
// ============================================
export interface LibrarySetting {
  id: number
  settingKey: string
  settingValue: string
  description?: string
}

// ============================================
// Reports
// ============================================
export interface MostBorrowedBook {
  bookId: number
  title: string
  isbn: string
  borrowCount: number
  rank: number
}

export interface ActiveMember {
  memberId: number
  membershipNumber: string
  fullName: string
  email: string
  borrowCount: number
  rank: number
}

export interface OverdueItem {
  borrowingId: number
  memberName: string
  membershipNumber: string
  memberEmail: string
  bookTitle: string
  barcode: string
  borrowDate: string
  dueDate: string
  overdueDays: number
  estimatedFine: number
}

export interface OverdueReport {
  totalOverdueBorrowings: number
  totalOverdueFines: number
  overdueList: OverdueItem[]
}

export interface DailyCollection {
  date: string
  amount: number
  paymentCount: number
}

export interface FineCollectionReport {
  startDate: string
  endDate: string
  totalCollected: number
  totalPayments: number
  dailyCollections: DailyCollection[]
}

export interface InventoryReport {
  totalBooks: number
  totalCopies: number
  availableCopies: number
  borrowedCopies: number
  damagedCopies: number
  lostCopies: number
}

export interface Publisher {
  id: number
  name: string
  address?: string
  phone?: string
  email?: string
  website?: string
}

// ============================================
// Book Copies
// ============================================
export interface BookCopy {
  id: number
  bookId?: number
  bookTitle?: string
  bookIsbn?: string
  barcode: string
  condition: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'DAMAGED'
  status: 'AVAILABLE' | 'BORROWED' | 'RESERVED' | 'LOST' | 'MAINTENANCE'
  acquisitionDate?: string
  price?: number
  shelfId?: number
  shelfCode?: string
  branchId?: number
  branchName?: string
  sectionName?: string
  floorName?: string
  createdAt: string
}

export interface BookCopyCreateRequest {
  bookId: number
  barcode?: string
  shelfId?: number
  branchId?: number
  condition: 'EXCELLENT' | 'GOOD' | 'FAIR' | 'POOR' | 'DAMAGED'
  acquisitionDate?: string
  price?: number
}

export interface Shelf {
  id: number
  shelfCode: string
  capacity?: number
  sectionId?: number
  sectionName?: string
  sectionCode?: string
}

export interface LibraryBranch {
  id: number
  name: string
  address: string
  phone?: string
  email?: string
  isActive: boolean
}

export interface Notification {
  id: number
  userId?: number
  title: string
  message: string
  type: string
  isRead: boolean
  sentVia?: string
  createdAt: string
}