/// <reference types="vitest" />
import { defineConfig, loadEnv } from 'vite';
import basicSsl from '@vitejs/plugin-basic-ssl';

const DEV_PORT = 5173;
const DEFAULT_API_TARGET = 'http://localhost:3001';

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiTarget = env.FLUX_API_PROXY_TARGET || DEFAULT_API_TARGET;
  const isLan = mode === 'lan';

  return {
    plugins: [...(isLan ? [basicSsl()] : [])],
    server: {
      port: DEV_PORT,
      strictPort: true,
      host: isLan ? '0.0.0.0' : 'localhost',
      proxy: {
        '/api': { target: apiTarget, changeOrigin: true },
        '/socket.io': { target: apiTarget, ws: true, changeOrigin: true },
      },
    },
    build: {
      target: 'es2022',
      sourcemap: false,
    },
    test: {
      globals: true,
      environment: 'happy-dom',
      include: ['src/**/*.test.ts'],
      restoreMocks: true,
    },
  };
});
