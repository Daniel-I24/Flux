import { describe, it, expect } from 'vitest';
import { formatDateTime, isPastDate, addMinutes } from './dateUtils';

describe('Date Utilities', () => {
  it('should format ISO string to readable localized format', () => {
    const iso = '2026-10-19T08:00:00.000Z';
    const formatted = formatDateTime(iso, 'es-ES');
    expect(formatted.length).toBeGreaterThan(5);
  });

  it('should correctly identify past dates', () => {
    const past = new Date(Date.now() - 10000).toISOString();
    const future = new Date(Date.now() + 10000).toISOString();
    expect(isPastDate(past)).toBe(true);
    expect(isPastDate(future)).toBe(false);
  });

  it('should add minutes accurately', () => {
    const now = new Date('2026-10-18T10:00:00.000Z');
    const plusFive = addMinutes(now, 5);
    expect(plusFive.toISOString()).toBe('2026-10-18T10:05:00.000Z');
  });
});
