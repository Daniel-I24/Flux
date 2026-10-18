export type ErrorCode =
  | 'VALIDATION'
  | 'NETWORK'
  | 'TIMEOUT'
  | 'CLASSIFICATION'
  | 'STORAGE'
  | 'SPEECH_UNSUPPORTED'
  | 'PERMISSION_DENIED'
  | 'SYNC'
  | 'NOT_FOUND'
  | 'UNKNOWN';

/** Base class of the error hierarchy. `code` maps to a translated user message. */
export class AppError extends Error {
  constructor(
    message: string,
    public readonly code: ErrorCode = 'UNKNOWN',
    options?: { cause?: unknown }
  ) {
    super(message, options);
    this.name = new.target.name;
  }
}

export class ValidationError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 'VALIDATION', options);
  }
}

export class NetworkError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 'NETWORK', options);
  }
}

export class TimeoutError extends AppError {
  constructor(message: string) {
    super(message, 'TIMEOUT');
  }
}

export class ClassificationError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 'CLASSIFICATION', options);
  }
}

export class StorageError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 'STORAGE', options);
  }
}

export class SpeechError extends AppError {
  constructor(
    message: string,
    code: 'SPEECH_UNSUPPORTED' | 'PERMISSION_DENIED' = 'SPEECH_UNSUPPORTED'
  ) {
    super(message, code);
  }
}

export class SyncError extends AppError {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, 'SYNC', options);
  }
}

export class NotFoundError extends AppError {
  constructor(message: string) {
    super(message, 'NOT_FOUND');
  }
}

export const toAppError = (error: unknown): AppError => {
  if (error instanceof AppError) return error;
  if (error instanceof Error) return new AppError(error.message, 'UNKNOWN', { cause: error });
  return new AppError(String(error));
};
