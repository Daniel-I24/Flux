/**
 * Thin logging facade: the only place allowed to touch the console.
 * Silent during unit tests to keep the output readable.
 */
export interface Logger {
  warn(message: string, details?: unknown): void;
  error(message: string, details?: unknown): void;
}

const isSilent = import.meta.env.MODE === 'test';
const PREFIX = '[flux]';

export const logger: Logger = {
  warn(message, details) {
    if (!isSilent) console.warn(PREFIX, message, details ?? '');
  },
  error(message, details) {
    if (!isSilent) console.error(PREFIX, message, details ?? '');
  },
};
