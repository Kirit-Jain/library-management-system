import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { toast } from 'sonner'
import { Users, Settings as SettingsIcon, Lock, Unlock, Edit } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Badge } from '@/components/ui/badge'
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table'
import {
  Dialog, DialogContent, DialogFooter,
  DialogHeader, DialogTitle, DialogDescription
} from '@/components/ui/dialog'
import { Label } from '@/components/ui/labels'
import { adminApi } from '@/api/admin'
import type { LibrarySetting } from '@/types'

export function AdminPage() {
  const queryClient = useQueryClient()
  const [activeTab, setActiveTab] = useState<'users' | 'settings'>('users')
  const [editingSetting, setEditingSetting] = useState<LibrarySetting | null>(null)
  const [settingValue, setSettingValue] = useState('')

  const { data: usersData } = useQuery({
    queryKey: ['admin-users'],
    queryFn: () => adminApi.getUsers(0, 100),
  })

  const { data: settings } = useQuery({
    queryKey: ['admin-settings'],
    queryFn: adminApi.getSettings,
  })

  const lockMutation = useMutation({
    mutationFn: adminApi.lockUser,
    onSuccess: () => {
      toast.success('User locked')
      queryClient.invalidateQueries({ queryKey: ['admin-users'] })
    },
  })

  const unlockMutation = useMutation({
    mutationFn: adminApi.unlockUser,
    onSuccess: () => {
      toast.success('User unlocked')
      queryClient.invalidateQueries({ queryKey: ['admin-users'] })
    },
  })

  const updateSettingMutation = useMutation({
    mutationFn: ({ key, value }: { key: string; value: string }) =>
      adminApi.updateSetting(key, value),
    onSuccess: () => {
      toast.success('Setting updated')
      queryClient.invalidateQueries({ queryKey: ['admin-settings'] })
      setEditingSetting(null)
    },
  })

  return (
    <div className="space-y-6 animate-fade-in">
      <div>
        <h1 className="text-3xl font-bold">Admin Panel</h1>
        <p className="text-muted-foreground">Manage users and system settings</p>
      </div>

      {/* Tabs */}
      <div className="flex gap-2 border-b">
        <button
          onClick={() => setActiveTab('users')}
          className={`px-4 py-2 font-medium border-b-2 transition-colors ${
            activeTab === 'users'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground'
          }`}
        >
          <Users className="w-4 h-4 inline mr-2" />
          Users
        </button>
        <button
          onClick={() => setActiveTab('settings')}
          className={`px-4 py-2 font-medium border-b-2 transition-colors ${
            activeTab === 'settings'
              ? 'border-primary text-primary'
              : 'border-transparent text-muted-foreground'
          }`}
        >
          <SettingsIcon className="w-4 h-4 inline mr-2" />
          Settings
        </button>
      </div>

      {activeTab === 'users' && (
        <Card>
          <CardHeader>
            <CardTitle>User Management</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>ID</TableHead>
                  <TableHead>Username</TableHead>
                  <TableHead>Email</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {usersData?.content?.map((user) => (
                  <TableRow key={user.id}>
                    <TableCell>{user.id}</TableCell>
                    <TableCell className="font-medium">{user.username}</TableCell>
                    <TableCell>{user.email}</TableCell>
                    <TableCell>
                      <div className="flex gap-1">
                        {user.isActive ? (
                          <Badge variant="success">Active</Badge>
                        ) : (
                          <Badge variant="secondary">Inactive</Badge>
                        )}
                        {user.isLocked && <Badge variant="destructive">Locked</Badge>}
                      </div>
                    </TableCell>
                    <TableCell>
                      <div className="flex gap-2">
                        {user.isLocked ? (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => unlockMutation.mutate(user.id)}
                          >
                            <Unlock className="w-3 h-3 mr-1" />
                            Unlock
                          </Button>
                        ) : (
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() => lockMutation.mutate(user.id)}
                          >
                            <Lock className="w-3 h-3 mr-1" />
                            Lock
                          </Button>
                        )}
                      </div>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {activeTab === 'settings' && (
        <Card>
          <CardHeader>
            <CardTitle>Library Settings</CardTitle>
          </CardHeader>
          <CardContent>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Key</TableHead>
                  <TableHead>Value</TableHead>
                  <TableHead>Description</TableHead>
                  <TableHead>Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {settings?.map((setting) => (
                  <TableRow key={setting.id}>
                    <TableCell className="font-mono text-sm">
                      {setting.settingKey}
                    </TableCell>
                    <TableCell className="font-medium">
                      {setting.settingValue}
                    </TableCell>
                    <TableCell className="text-sm text-muted-foreground">
                      {setting.description}
                    </TableCell>
                    <TableCell>
                      <Button
                        size="sm"
                        variant="outline"
                        onClick={() => {
                          setEditingSetting(setting)
                          setSettingValue(setting.settingValue)
                        }}
                      >
                        <Edit className="w-3 h-3 mr-1" />
                        Edit
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </CardContent>
        </Card>
      )}

      {/* Edit Setting Dialog */}
      <Dialog open={!!editingSetting} onOpenChange={(open) => !open && setEditingSetting(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Edit Setting</DialogTitle>
            <DialogDescription>
              Key: <code className="font-mono">{editingSetting?.settingKey}</code>
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div className="space-y-2">
              <Label>Value</Label>
              <Input
                value={settingValue}
                onChange={(e) => setSettingValue(e.target.value)}
              />
            </div>
            <p className="text-sm text-muted-foreground">
              {editingSetting?.description}
            </p>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditingSetting(null)}>
              Cancel
            </Button>
            <Button
              onClick={() => updateSettingMutation.mutate({
                key: editingSetting!.settingKey,
                value: settingValue,
              })}
              disabled={updateSettingMutation.isPending}
            >
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
