import { ClassificationResult } from '../types/global';

export interface NotePayload {
  id: string;
  rawText: string;
  title: string;
  content: string;
  category: string;
  reminderAt: string | null;
  createdAt: string;
  isCompleted: boolean;
  syncStatus: string;
}

export interface AppEvents {
  NOTE_CAPTURED: { rawText: string };
  NOTE_CLASSIFIED: { result: ClassificationResult };
  NOTE_SYNC_START: { payload: Partial<NotePayload> };
  NOTE_SYNCED: { note: NotePayload };
  CONNECTION_CHANGED: { isConnected: boolean };
  THEME_CHANGED: { mode: 'light' | 'dark' };
  LOCALE_CHANGED: { locale: string };
  ALARM_TRIGGERED: { note: NotePayload };
  REMINDER_SNOOZED: { noteId: string; minutes: number };
  REMINDER_DISMISSED: { noteId: string };
}

export type EventKey = keyof AppEvents;
export type EventHandler<K extends EventKey> = (data: AppEvents[K]) => void;
