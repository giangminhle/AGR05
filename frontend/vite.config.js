import { defineConfig, loadEnv } from 'vite';
import react from '@vitejs/plugin-react';

// Gọi /api chuyển sang Spring Boot (mặc định cổng 8080; đổi bằng AGR05_API trong file .env.local).
// Cùng nguồn nên cookie phiên dùng được.
export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  return {
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': env.AGR05_API || 'http://localhost:8080',
    },
  },
  build: {
    chunkSizeWarningLimit: 1200,
  },
  };
});
