/** Cross-cutting primitive types shared by every module. */

export const NOTE_CATEGORIES = ['task', 'idea', 'reminder', 'info'] as const;
export type NoteCategory = (typeof NOTE_CATEGORIES)[number];

export const DEVICE_ROLES = ['watch', 'phone'] as const;
export type DeviceRole = (typeof DEVICE_ROLES)[number];

export type AiEngine = 'remote' | 'local';
export type ConnectionStatus = 'connected' | 'reconnecting' | 'offline';
export type Locale = 'es' | 'en';
export type ThemeMode = 'light' | 'dark';
export type CaptureMode = 'voice' | 'text';

export type Unsubscribe = () => void;
