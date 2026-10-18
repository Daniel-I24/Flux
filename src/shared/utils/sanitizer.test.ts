import { describe, it, expect } from 'vitest';
import { sanitizeText, truncateText, cleanSpokenFillers } from './sanitizer';

describe('Sanitizer Utilities', () => {
  it('should escape dangerous HTML characters to prevent XSS', () => {
    const malicious = '<script>alert("xss")</script>&foo=\'bar\'';
    const cleaned = sanitizeText(malicious);
    expect(cleaned).not.toContain('<script>');
    expect(cleaned).toContain('&lt;script&gt;');
    expect(cleaned).toContain('&amp;foo=&#x27;bar&#x27;');
  });

  it('should truncate text without breaking words if possible', () => {
    const text = 'Esta es una nota larga para recortar';
    expect(truncateText(text, 10)).toBe('Esta es un...');
    expect(truncateText('Corta', 10)).toBe('Corta');
  });

  it('should strip common Spanish spoken filler prefixes', () => {
    expect(cleanSpokenFillers('Recuérdame entregar el reporte')).toBe('entregar el reporte');
    expect(cleanSpokenFillers('Por favor avísame que tengo que salir')).toBe('salir');
  });
});
