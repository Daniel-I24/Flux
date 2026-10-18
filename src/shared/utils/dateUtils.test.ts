import { describe, expect, it } from 'vitest';
import { addMinutes, formatRelativeTime, HOUR_MS, parseIsoDate } from './dateUtils';

describe('dateUtils', () => {
  const now = new Date('2026-10-18T10:00:00');

  it('adds minutes without mutating the input', () => {
    const later = addMinutes(now, 5);
    expect(later.getTime() - now.getTime()).toBe(5 * 60 * 1000);
    expect(now.toISOString()).toBe(new Date('2026-10-18T10:00:00').toISOString());
  });

  it('parses ISO strings defensively', () => {
    expect(parseIsoDate('not-a-date')).toBeNull();
    expect(parseIsoDate(null)).toBeNull();
    expect(parseIsoDate(now.toISOString())?.getTime()).toBe(now.getTime());
  });

  it('formats relative time in Spanish', () => {
    expect(formatRelativeTime(addMinutes(now, 5), now, 'es-CO')).toBe('dentro de 5 minutos');
    expect(formatRelativeTime(new Date(now.getTime() - 2 * HOUR_MS), now, 'es-CO')).toBe(
      'hace 2 horas'
    );
  });
});
