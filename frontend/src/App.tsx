import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { Toaster } from 'sonner'
import { useAuthStore } from '@/store/auth'

import { LoginPage } from '@/pages/auth/LoginPage'
import { RegisterPage } from '@/pages/auth/RegisterPage'
import { DashboardPage } from '@/pages/dashboard/DashboardPage'
import { BooksPage } from '@/pages/books/BooksPages'
import { MembersPage } from '@/pages/members/MembersPage'
import { MemberDetailPage } from '@/pages/members/MembersDetailPage'
import { BorrowingsPage } from '@/pages/borrowings/BorrowingsPage'
import { FinesPage } from '@/pages/fines/FinesPage'
import { NotificationsPage } from '@/pages/notifications/NotificationsPage'
import { ReportsPage } from '@/pages/reports/ReportsPage'
import { AdminPage } from '@/pages/admin/AdminPage'
import { BorrowRequestsPage } from '@/pages/admin/BorrowRequestPage'
import { MyDashboardPage } from '@/pages/members/MyDashBoardPage'
import { MyProfilePage } from '@/pages/members/MyProfilePage'
import { MyBorrowingsPage } from '@/pages/members/MyBorrowingsPage'
import { MyFinesPage } from '@/pages/members/MyFinesPage'
import { MyRequestsPage } from '@/pages/members/MyRequestsPage'
import { MainLayout } from '@/components/layout/MainLayout'
import { BookCopiesPage } from '@/pages/admin/BookCopiesPage'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 1000 * 60 * 5,
      retry: 1,
      refetchOnWindowFocus: false,
    },
  },
})

function ProtectedRoute({ children, roles }: { children: React.ReactNode; roles?: string[] }) {
  const { isAuthenticated, user } = useAuthStore()

  if (!isAuthenticated) return <Navigate to="/login" />

  if (roles && !roles.includes(user?.role || '')) {
    return <Navigate to="/" />
  }

  return <>{children}</>
}

function HomeRedirect() {
  const { user } = useAuthStore()
  const isAdmin = user?.role === 'ADMIN' || user?.role === 'LIBRARIAN'
  return <Navigate to={isAdmin ? '/dashboard' : '/my-dashboard'} />
}

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />

          <Route
            path="/"
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<HomeRedirect />} />

            {/* Common Routes */}
            <Route path="books" element={<BooksPage />} />
            <Route path="notifications" element={<NotificationsPage />} />

            {/* Admin/Librarian Only */}
            <Route path="dashboard" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <DashboardPage />
              </ProtectedRoute>
            } />
            <Route path="members" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <MembersPage />
              </ProtectedRoute>
            } />
            <Route path="members/:id" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <MemberDetailPage />
              </ProtectedRoute>
            } />
            <Route path="borrow-requests" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <BorrowRequestsPage />
              </ProtectedRoute>
            } />
            <Route path="borrowings" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <BorrowingsPage />
              </ProtectedRoute>
            } />
            <Route path="fines" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <FinesPage />
              </ProtectedRoute>
            } />
            <Route path="reports" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <ReportsPage />
              </ProtectedRoute>
            } />
            <Route path="admin" element={
              <ProtectedRoute roles={['ADMIN']}>
                <AdminPage />
              </ProtectedRoute>
            } />

            <Route path="book-copies" element={
              <ProtectedRoute roles={['ADMIN', 'LIBRARIAN']}>
                <BookCopiesPage />
              </ProtectedRoute>
            } />

            {/* Member Only */}
            <Route path="my-dashboard" element={<MyDashboardPage />} />
            <Route path="my-profile" element={<MyProfilePage />} />
            <Route path="my-borrowings" element={<MyBorrowingsPage />} />
            <Route path="my-fines" element={<MyFinesPage />} />
            <Route path="my-requests" element={<MyRequestsPage />} />
          </Route>

          <Route path="*" element={<Navigate to="/" />} />
        </Routes>

        <Toaster position="top-right" richColors closeButton />
      </BrowserRouter>
    </QueryClientProvider>
  )
}

export default App