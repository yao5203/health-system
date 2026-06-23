import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/user-center': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/doctor': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/health': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/user': {
        target: 'http://localhost:8080',
        changeOrigin: true
      },
      '/question': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})
