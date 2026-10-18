/**
 * Standardized Application Error hierarchy.
 */

export type ErrorCode =
  | 'VALIDATION_ERROR'
  | 'NETWORK_ERROR'
  | 'AI_CLASSIFICATION_ERROR'
  | 'STORAGE_ERROR'
  | 'SPEECH_NOT_SUPPORTED'
  | 'MICROPHONE_PERMISSION_DENIED'
  | 'SYNC_ERROR'
  | 'NOTIFICATION_PERMISSION_DENIED';

export class AppError extends Error {
  public readonly code: ErrorCode;
  public readonly isOperational: boolean;
  public readonly timestamp: string;

  constructor(message: string, code: ErrorCode, isOperational = true) {
    super(message);
    this.name = 'AppError';
    this.code = code;
    this.isOperational = isOperational;
    this.timestamp = new Date().toISOString();
    Object.setPrototypeOf(this, new.target.prototype);
  }
}
