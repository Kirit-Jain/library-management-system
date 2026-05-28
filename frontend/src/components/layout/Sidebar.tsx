import { NavLink } from 'react-router-dom'
import {
  LayoutDashboard,
  BookOpen,
  Users,
  ArrowRightLeft,
  IndianRupee,
  FileText,
  Settings,
  Bell,
  Library,
  User,
  ClipboardList,
  BookMarked,
  LucideIcon,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { useAuthStore } from '@/store/auth'

interface NavItem {
  icon: LucideIcon
  label: string
  path: string
  roles?: string[]
}

const adminNavItems: NavItem[] = [
  { icon: LayoutDashboard, label: 'Dashboard', path: '/dashboard' },
  { icon: BookOpen, label: 'Books', path: '/books' },
  { icon: BookMarked, label: 'Book Copies', path: '/book-copies' },
  { icon: Users, label: 'Members', path: '/members' },
  { icon: ClipboardList, label: 'Borrow Requests', path: '/borrow-requests' },
  { icon: ArrowRightLeft, label: 'Borrowings', path: '/borrowings' },
  { icon: IndianRupee, label: 'Fines', path: '/fines' },
  { icon: Bell, label: 'Notifications', path: '/notifications' },
  { icon: FileText, label: 'Reports', path: '/reports' },
  { icon: Settings, label: 'Admin', path: '/admin', roles: ['ADMIN'] },
]

const memberNavItems: NavItem[] = [
  { icon: LayoutDashboard, label: 'My Dashboard', path: '/my-dashboard' },
  { icon: User, label: 'My Profile', path: '/my-profile' },
  { icon: BookOpen, label: 'Browse Books', path: '/books' },
  { icon: ArrowRightLeft, label: 'My Borrowings', path: '/my-borrowings' },
  { icon: ClipboardList, label: 'My Requests', path: '/my-requests' },
  { icon: IndianRupee, label: 'My Fines', path: '/my-fines' },
  { icon: Bell, label: 'Notifications', path: '/notifications' },
]

export function Sidebar() {
  const { user } = useAuthStore()

  const isAdmin = user?.role === 'ADMIN' || user?.role === 'LIBRARIAN'
  const navItems = isAdmin ? adminNavItems : memberNavItems

  const filteredItems = navItems.filter(
    (item) => !item.roles || item.roles.includes(user?.role || '')
  )

  return (
    <aside className="w-64 bg-card border-r min-h-screen flex flex-col">
      <div className="p-6 border-b">
        <div className="flex items-center gap-2">
          <Library className="w-8 h-8 text-primary" />
          <div>
            <h2 className="text-lg font-bold">Library</h2>
            <p className="text-xs text-muted-foreground">Management System</p>
          </div>
        </div>
      </div>

      <nav className="flex-1 p-4 space-y-1">
        {filteredItems.map((item) => (
          <NavLink
            key={item.path}
            to={item.path}
            className={({ isActive }) =>
              cn(
                'flex items-center gap-3 px-3 py-2 rounded-lg text-sm font-medium transition-colors',
                isActive
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-foreground'
              )
            }
          >
            <item.icon className="w-4 h-4" />
            {item.label}
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-primary text-primary-foreground flex items-center justify-center font-semibold">
            {user?.fullName?.charAt(0) || 'U'}
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-sm font-medium truncate">{user?.fullName}</p>
            <p className="text-xs text-muted-foreground">{user?.role}</p>
          </div>
        </div>
      </div>
    </aside>
  )
}