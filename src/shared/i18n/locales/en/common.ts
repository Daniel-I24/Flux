import type { esCommon } from '../es/common';
import type { DeepStrings } from '../../types';

export const enCommon: DeepStrings<typeof esCommon> = {
  app: {
    name: 'FLUX',
    tagline: 'Voice capture for your watch and your phone',
    loading: 'Loading…',
    routeError: 'This view could not be loaded.',
    skipToContent: 'Skip to content',
    backToDemo: 'Back to the demo',
  },
  actions: {
    save: 'Save',
    cancel: 'Cancel',
    edit: 'Edit',
    delete: 'Delete',
    complete: 'Complete',
    close: 'Close',
    retry: 'Retry',
    snooze: 'Snooze 5 min',
    back: 'Back',
  },
  categories: {
    all: 'All',
    task: 'Task',
    idea: 'Idea',
    reminder: 'Reminder',
    info: 'Info',
  },
  categoriesPlural: {
    all: 'All',
    task: 'Tasks',
    idea: 'Ideas',
    reminder: 'Reminders',
    info: 'Info',
  },
  connection: {
    connected: 'Connected',
    reconnecting: 'Reconnecting…',
    offline: 'Offline',
    localDemo: 'Local link',
    waitingPeer: 'Waiting for the other device',
    pending: '{count} queued',
  },
  ai: {
    remote: 'Remote AI · Gemini',
    local: 'Local AI',
    statusLabel: 'Classification engine',
  },
  errors: {
    VALIDATION: 'The data is not valid.',
    NETWORK: 'The server cannot be reached.',
    TIMEOUT: 'The operation took too long.',
    CLASSIFICATION: 'The note could not be interpreted.',
    STORAGE: 'Could not save on this device.',
    SPEECH_UNSUPPORTED: 'This browser does not support dictation. Type your note.',
    PERMISSION_DENIED: 'Microphone permission denied. Type your note.',
    SYNC: 'Could not synchronize.',
    NOT_FOUND: 'Item not found.',
    UNKNOWN: 'An unexpected error occurred.',
  },
  settings: {
    toLight: 'Switch to light theme',
    toDark: 'Switch to dark theme',
    switchLanguage: 'Switch language to Spanish',
    languageShort: 'EN',
  },
  time: {
    noDate: 'No date',
  },
  notifications: {
    title: 'FLUX · Reminder',
  },
};
