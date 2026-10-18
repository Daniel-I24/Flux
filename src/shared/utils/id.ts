/** Unambiguous alphabet for pairing codes (no 0/O, 1/I/L). */
export const PAIR_CODE_ALPHABET = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789';

const randomIndexes = (count: number, max: number): number[] => {
  const values = new Uint32Array(count);
  globalThis.crypto.getRandomValues(values);
  return [...values].map((value) => value % max);
};

export const createId = (): string => {
  if (typeof globalThis.crypto?.randomUUID === 'function') return globalThis.crypto.randomUUID();
  const hex = randomIndexes(16, 16).map((value) => value.toString(16));
  return `${Date.now().toString(36)}-${hex.join('')}`;
};

export const createPairCode = (length: number): string =>
  randomIndexes(length, PAIR_CODE_ALPHABET.length)
    .map((index) => PAIR_CODE_ALPHABET.charAt(index))
    .join('');

export const normalizePairCode = (input: string): string =>
  input.toUpperCase().replace(/[^A-Z0-9]/g, '');

export const isValidPairCode = (input: string, length: number): boolean =>
  new RegExp(`^[A-Z0-9]{${length}}$`).test(input);
