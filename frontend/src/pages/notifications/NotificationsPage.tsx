import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Bell, Check, CheckCheck } from 'lucide-react'
import { Card, CardContent } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Badge } from '@/components/ui/badge'
import { notificationsApi } from '@/api/notification'
import { useAuthStore } from '@/store/auth'
import { formatDateTime } from '@/lib/utils'

export function NotificationsPage() {
  const queryClient = useQueryClient()
  const { user } = useAuthStore()

  const { data, isLoading } = useQuery({
    queryKey: ['notifications', user?.id],
    queryFn: () => notificationsApi.getUserNotifications(user!.id),
    enabled: !!user?.id,
  })

  const { data: unreadCount } = useQuery({
    queryKey: ['unread-count', user?.id],
    queryFn: () => notificationsApi.getUnreadCount(user!.id),
    enabled: !!user?.id,
  })

  const markReadMutation = useMutation({
    mutationFn: notificationsApi.markAsRead,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['unread-count'] })
    },
  })

  const markAllReadMutation = useMutation({
    mutationFn: () => notificationsApi.markAllAsRead(user!.id),
    onSuccess: () => {
      toast.success('All notifications marked as read')
      queryClient.invalidateQueries({ queryKey: ['notifications'] })
      queryClient.invalidateQueries({ queryKey: ['unread-count'] })
    },
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold">Notifications</h1>
          <p className="text-muted-foreground">
            {unreadCount && unreadCount > 0
              ? `You have ${unreadCount} unread notification${unreadCount > 1 ? 's' : ''}`
              : 'All caught up!'}
          </p>
        </div>
        {unreadCount && unreadCount > 0 && (
          <Button onClick={() => markAllReadMutation.mutate()}>
            <CheckCheck className="w-4 h-4 mr-2" />
            Mark All Read
          </Button>
        )}
      </div>

      <Card>
        <CardContent className="p-0">
          {isLoading ? (
            <div className="p-8 text-center">
              <p className="text-muted-foreground">Loading notifications...</p>
            </div>
          ) : !data?.content?.length ? (
            <div className="p-12 text-center">
              <Bell className="w-12 h-12 mx-auto text-muted-foreground mb-4" />
              <p className="text-muted-foreground">No notifications yet</p>
              <p className="text-sm text-muted-foreground mt-2">
                You'll see notifications about your borrowings, fines, and more here
              </p>
            </div>
          ) : (
            <div className="divide-y">
              {data.content.map((notification) => (
                <div
                  key={notification.id}
                  className={`p-4 hover:bg-accent transition-colors ${
                    !notification.isRead ? 'bg-blue-50/50' : ''
                  }`}
                >
                  <div className="flex items-start justify-between gap-3">
                    <div className="flex-1 space-y-1">
                      <div className="flex items-center gap-2">
                        <h3 className="font-medium">{notification.title}</h3>
                        {!notification.isRead && (
                          <Badge variant="default" className="text-xs">
                            New
                          </Badge>
                        )}
                        <Badge variant="outline" className="text-xs">
                          {notification.type}
                        </Badge>
                      </div>
                      <p className="text-sm text-muted-foreground">
                        {notification.message}
                      </p>
                      <p className="text-xs text-muted-foreground">
                        {formatDateTime(notification.createdAt)}
                      </p>
                    </div>
                    {!notification.isRead && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => markReadMutation.mutate(notification.id)}
                      >
                        <Check className="w-4 h-4" />
                      </Button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}