import type { ZodType } from 'zod';
import { StorageError } from '../../core/errors/AppError';
import { logger } from '../../core/logger/logger';

/** Minimal storage abstraction so services never touch `localStorage` directly. */
export interface KeyValueStore {
  get(key: string): string | null;
  set(key: string, value: string): void;
  remove(key: string): void;
}

export class BrowserStorage implements KeyValueStore {
  constructor(private readonly resolveStorage: () => Storage = () => window.localStorage) {}

  get(key: string): string | null {
    try {
      return this.resolveStorage().getItem(key);
    } catch (error) {
      logger.warn(`Storage read failed for "${key}"`, error);
      return null;
    }
  }

  set(key: string, value: string): void {
    try {
      this.resolveStorage().setItem(key, value);
    } catch (error) {
      throw new StorageError(`Storage write failed for "${key}"`, { cause: error });
    }
  }

  remove(key: string): void {
    try {
      this.resolveStorage().removeItem(key);
    } catch (error) {
      logger.warn(`Storage remove failed for "${key}"`, error);
    }
  }
}

export class MemoryStorage implements KeyValueStore {
  private readonly values = new Map<string, string>();

  get(key: string): string | null {
    return this.values.get(key) ?? null;
  }

  set(key: string, value: string): void {
    this.values.set(key, value);
  }

  remove(key: string): void {
    this.values.delete(key);
  }
}

/** Reads JSON and validates it; corrupted or invalid data yields `null` instead of throwing. */
export const readValidatedJson = <T>(store: KeyValueStore, key: string, schema: ZodType<T>): T | null => {
  const raw = store.get(key);
  if (raw === null) return null;
  try {
    const result = schema.safeParse(JSON.parse(raw));
    if (result.success) return result.data;
    logger.warn(`Invalid data discarded for "${key}"`, result.error.issues);
  } catch (error) {
    logger.warn(`Corrupted JSON discarded for "${key}"`, error);
  }
  return null;
};

export const writeJson = (store: KeyValueStore, key: string, value: unknown): void =>
  store.set(key, JSON.stringify(value));

export const browserStorage = new BrowserStorage();
