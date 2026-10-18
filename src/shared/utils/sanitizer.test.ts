import { describe, expect, it } from 'vitest';
import { capitalize, normalizeForSearch, sanitizeUserText, truncate } from './sanitizer';

describe('sanitizeUserText', () => {
  it('removes markup tags, control and invisible characters', () => {
    const dirty = '  <script>alert(1)</script>Hola\u0007 \u200Bmundo  ';
    expect(sanitizeUserText(dirty)).toBe('alert(1)Hola mundo');
  });

  it('collapses whitespace and line breaks', () => {
    expect(sanitizeUserText('comprar\n\n  pan\t hoy')).toBe('comprar pan hoy');
  });

  it('caps the length', () => {
    expect(sanitizeUserText('a'.repeat(50), 10)).toHaveLength(10);
  });

  it('returns an empty string for non-string input', () => {
    expect(sanitizeUserText(42)).toBe('');
    expect(sanitizeUserText(undefined)).toBe('');
  });
});

describe('text helpers', () => {
  it('truncates with an ellipsis', () => {
    expect(truncate('Entregar proyecto final', 10)).toBe('Entregar…');
    expect(truncate('corto', 10)).toBe('corto');
  });

  it('capitalizes the first letter', () => {
    expect(capitalize('ñandú')).toBe('Ñandú');
  });

  it('normalizes accents for search', () => {
    expect(normalizeForSearch('Recuérdame MAÑANA')).toBe('recuerdame manana');
  });
});
