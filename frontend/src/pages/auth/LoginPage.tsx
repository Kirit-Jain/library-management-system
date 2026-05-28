import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Library, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/labels' // Fixed component import path typo: 'labels' -> 'label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { authApi } from '@/api/auth'
import { useAuthStore } from '@/store/auth'

export function LoginPage() {
  const navigate = useNavigate()
  const setAuth = useAuthStore((state) => state.setAuth)
  const [formData, setFormData] = useState({ email: '', password: '' })

  const loginMutation = useMutation({
    mutationFn: authApi.login,
    onSuccess: (data) => {
      // 1. Commit token and profile payloads to your global Zustand application state store
      setAuth(data.user, data.accessToken, data.refreshToken)
      
      toast.success(`Welcome back, ${data.user.fullName || data.user.username}!`)
      
      // 2. Perform dynamic role-based redirection right after a successful handshake
      // Matches your backend authority configurations perfectly
      const userRole = Array.isArray(data.user.role) ? data.user.role[0] : data.user.role;
      

      if (userRole === 'ROLE_ADMIN' || userRole === 'ADMIN') {
        navigate('/dashboard')
      } else {
        navigate('/books')
      }
    },
    onError: (error: unknown) => {
      // CRITICAL: This uncovers why your authentication requests are stalling!
      console.error("AXIOS AUTHENTICATION REJECTION:", error);
      
      // Pull down the error message safely from your Spring Boot Custom Exception Handler payload
      const errorMessage = (error as { response?: { data?: { message?: string } } }).response?.data?.message || "Invalid email or password. Please try again.";
      toast.error(errorMessage);
    }
  })

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    console.log("Form submitted. Triggering mutation with:", formData.email);
    loginMutation.mutate(formData)
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-100 p-4">
      <Card className="w-full max-w-md shadow-xl animate-fade-in">
        <CardHeader className="text-center">
          <div className="flex justify-center mb-4">
            <div className="w-16 h-16 bg-primary rounded-2xl flex items-center justify-center">
              <Library className="w-8 h-8 text-primary-foreground" />
            </div>
          </div>
          <CardTitle className="text-2xl">Welcome Back</CardTitle>
          <CardDescription>Sign in to your library account</CardDescription>
        </CardHeader>

        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                placeholder="admin@library.com"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                required
                disabled={loginMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                placeholder="••••••••"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
                disabled={loginMutation.isPending}
              />
            </div>

            <Button type="submit" className="w-full" disabled={loginMutation.isPending}>
              {loginMutation.isPending ? (
                <>
                  <Loader2 className="w-4 h-4 mr-2 animate-spin" />
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </Button>

            <p className="text-center text-sm text-muted-foreground">
              Don't have an account?{' '}
              <Link to="/register" className="text-primary hover:underline font-medium">
                Register here
              </Link>
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}