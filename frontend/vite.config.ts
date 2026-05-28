import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

export default defineConfig(({ mode }) => {
  // Load env file based on mode
  const env = loadEnv(mode, process.cwd(), '')

  return {
    plugins: [react()],
    resolve: {
      alias: {
        "@": path.resolve(__dirname, "./src"),
      },
    },
    server: {
      port: 3001,
      proxy: {
        '/api': {
          target: env.VITE_API_URL?.replace('/api/v1', '') || 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})