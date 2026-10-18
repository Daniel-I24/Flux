import { registerSW } from 'virtual:pwa-register';
import { logger } from '../core/logger/logger';

/** Installs the PWA service worker (offline shell, installability, notifications on Android). */
export const registerServiceWorker = (): void => {
  if (!('serviceWorker' in navigator)) return;
  registerSW({
    immediate: true,
    onRegisterError: (error: unknown) => logger.warn('Service worker registration failed', error),
  });
};
