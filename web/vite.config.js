import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// 화면(5173)에서 /api를 서버(8080)로 넘겨 줘서 출처 문제(CORS)를 피해요.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: {
      '/api': 'http://127.0.0.1:8080',
    },
  },
})
