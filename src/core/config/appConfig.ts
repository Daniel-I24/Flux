/**
 * Centralized Application Configuration.
 */

export const APP_CONFIG = {
  app: {
    name: 'Flux',
    version: '1.0.0',
    defaultLanguage: 'es',
  },
  storage: {
    notesKey: 'flux_notes_v2',
    themeKey: 'flux_theme_mode',
    languageKey: 'flux_locale_preference',
  },
  api: {
    baseUrl: import.meta.env.VITE_API_BASE_URL || 'http://localhost:3001',
    timeoutMs: 8000,
  },
  watch: {
    autoSaveCountdownSeconds: 5,
    simulatedSyncLatencyMs: 250,
    waveformSampleCount: 18,
  },
  reminders: {
    checkIntervalMs: 3000,
    defaultSnoozeMinutes: 5,
  },
} as const;
