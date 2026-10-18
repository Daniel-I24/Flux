/// <reference types="vitest" />
import { defineConfig, loadEnv } from 'vite';
import basicSsl from '@vitejs/plugin-basic-ssl';
import { VitePWA } from 'vite-plugin-pwa';

const DEV_PORT = 5173;
const DEFAULT_API_TARGET = 'http://localhost:3001';
const BACKEND_PATHS = [/^\/api\//, /^\/socket\.io\//];

const pwa = VitePWA({
  registerType: 'autoUpdate',
  injectRegister: false,
  includeAssets: ['favicon.svg', 'icons/apple-touch-icon.png'],
  manifest: {
    name: 'FLUX · Notas por voz',
    short_name: 'FLUX',
    description: 'Captura notas por voz en tu reloj, clasificadas con IA y sincronizadas con tu celular.',
    lang: 'es-CO',
    start_url: '/phone',
    scope: '/',
    display: 'standalone',
    orientation: 'portrait',
    background_color: '#090b11',
    theme_color: '#090b11',
    icons: [
      { src: '/icons/icon-192.png', sizes: '192x192', type: 'image/png' },
      { src: '/icons/icon-512.png', sizes: '512x512', type: 'image/png' },
      { src: '/icons/maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
    ],
  },
  workbox: {
    globPatterns: ['**/*.{js,css,html,svg,png,woff2}'],
    navigateFallback: '/index.html',
    navigateFallbackDenylist: BACKEND_PATHS,
    cleanupOutdatedCaches: true,
  },
  devOptions: {
    enabled: true,
    type: 'module',
    navigateFallback: 'index.html',
    navigateFallbackAllowlist: [/^\/(demo|watch|phone)/],
  },
});

export default defineConfig(({ mode }) => {
  const env = loadEnv(mode, process.cwd(), '');
  const apiTarget = env.FLUX_API_PROXY_TARGET || DEFAULT_API_TARGET;
  const isLan = mode === 'lan';

  return {
    plugins: [pwa, ...(isLan ? [basicSsl()] : [])],
    server: {
      port: DEV_PORT,
      strictPort: true,
      host: isLan ? '0.0.0.0' : 'localhost',
      proxy: {
        '/api': { target: apiTarget, changeOrigin: true },
        '/socket.io': { target: apiTarget, ws: true, changeOrigin: true },
      },
    },
    preview: {
      port: 4173,
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
