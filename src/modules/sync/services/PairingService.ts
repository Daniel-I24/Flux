import { ValidationError } from '../../../core/errors/AppError';
import type { KeyValueStore } from '../../../shared/storage/KeyValueStore';
import { createPairCode, isValidPairCode, normalizePairCode } from '../../../shared/utils/id';

/** Owns the 6-character pairing code of one device and the URL encoded in the QR. */
export class PairingService {
  constructor(
    private readonly store: KeyValueStore,
    private readonly storageKey: string,
    private readonly codeLength: number
  ) {}

  getCode(): string | null {
    const saved = this.store.get(this.storageKey);
    return saved && isValidPairCode(saved, this.codeLength) ? saved : null;
  }

  getOrCreateCode(): string {
    return this.getCode() ?? this.regenerate();
  }

  regenerate(): string {
    const code = createPairCode(this.codeLength);
    this.store.set(this.storageKey, code);
    return code;
  }

  /** Normalizes user input ("abc-123" → "ABC123") and stores it, or throws ValidationError. */
  setCode(input: string): string {
    const code = normalizePairCode(input);
    if (!isValidPairCode(code, this.codeLength)) throw new ValidationError('Invalid pairing code');
    this.store.set(this.storageKey, code);
    return code;
  }

  clear(): void {
    this.store.remove(this.storageKey);
  }

  static buildPairingUrl(origin: string, code: string): string {
    return `${origin.replace(/\/+$/, '')}/phone?pair=${encodeURIComponent(code)}`;
  }
}
