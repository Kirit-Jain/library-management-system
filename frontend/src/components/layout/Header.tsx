import { useNavigate } from 'react-router-dom'
import { Bell, LogOut, Search } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { useAuthStore } from '@/store/auth'

export function Header() {
  const navigate = useNavigate()
  const { logout, user } = useAuthStore()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <header className="h-16 border-b bg-card px-6 flex items-center justify-between">
      <div className="flex-1 max-w-md">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-muted-foreground" />
          <Input
            placeholder="Search books, members..."
            className="pl-9"
          />
        </div>
      </div>

      <div className="flex items-center gap-3">
        <Button variant="ghost" size="icon">
          <Bell className="w-5 h-5" />
        </Button>

        <div className="text-right">
          <p className="text-sm font-medium">{user?.fullName}</p>
          <p className="text-xs text-muted-foreground">{user?.email}</p>
        </div>

        <Button variant="outline" size="icon" onClick={handleLogout}>
          <LogOut className="w-4 h-4" />
        </Button>
      </div>
    </header>
  )
}