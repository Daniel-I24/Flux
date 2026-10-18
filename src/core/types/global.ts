/**
 * Global Core Types for FLUX.
 */

export enum NoteCategory {
  TASK = 'TASK',
  IDEA = 'IDEA',
  REMINDER = 'REMINDER',
  INFO = 'INFO',
}

export enum SyncStatus {
  PENDING = 'PENDING',
  SYNCED = 'SYNCED',
  FAILED = 'FAILED',
}

export enum AiEngineType {
  REMOTE_GEMINI = 'REMOTE_GEMINI',
  LOCAL_NLP = 'LOCAL_NLP',
}

export interface ClassificationResult {
  category: NoteCategory;
  title: string;
  content: string;
  hasReminder: boolean;
  reminderAt: string | null; // ISO-8601 string or null
  confidence: number;
  engine: AiEngineType;
}
