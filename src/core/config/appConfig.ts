import type { Locale } from '../types/domain';

const env = import.meta.env;

const resolveLocale = (value: string | undefined): Locale => (value === 'en' ? 'en' : 'es');

/**
 * Single source of runtime configuration. Values come from Vite env vars
 * (see .env.example); an empty base URL means "same origin" (Vite proxy in dev).
 */
export const APP_CONFIG = {
  appName: 'FLUX',
  apiBaseUrl: env.VITE_API_BASE_URL ?? '',
  socketUrl: env.VITE_SOCKET_URL ?? '',
  /** Origin encoded in the pairing QR (set by `npm run dev:lan` to the LAN HTTPS URL). */
  publicOrigin: env.VITE_PUBLIC_ORIGIN ?? '',
  defaultLocale: resolveLocale(env.VITE_DEFAULT_LOCALE),
  speechLang: env.VITE_SPEECH_LANG ?? 'es-CO',
  timeouts: {
    classifyMs: 7000,
    healthMs: 2500,
    socketAckMs: 4000,
  },
  watch: {
    autoSaveSeconds: 5,
    savedScreenMs: 1800,
    simulatedWordMs: 160,
    maxNoteLength: 500,
    vibrationPattern: [60, 40, 60],
  },
  reminders: {
    tickMs: 1000,
    snoozeMinutes: 5,
  },
  sync: {
    pairCodeLength: 6,
    seenMessagesLimit: 500,
    demoRoomCode: 'DEMO00',
    channelPrefix: 'flux-sync',
    activityPulseMs: 900,
  },
  storageKeys: {
    notes: 'flux.notes.v3',
    theme: 'flux.theme',
    locale: 'flux.locale',
    captureMode: 'flux.captureMode',
    pairCode: 'flux.pairCode',
    deviceId: 'flux.deviceId',
    outbox: 'flux.outbox',
  },
} as const;
