/**
 * Text sanitization and safety utilities to prevent XSS.
 */

export function sanitizeText(input: string): string {
  if (!input) return '';
  return input
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#x27;');
}

export function truncateText(text: string, maxLength: number): string {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return text.slice(0, maxLength).trimEnd() + '...';
}

export function cleanSpokenFillers(text: string): string {
  if (!text) return '';
  return text
    .replace(/^(por favor\s+)?(recuérdame|recuerdame|avísame|avisame|anota|guarda|registra)\s+(que\s+)?/i, '')
    .replace(/^(tengo que|debo|hay que)\s+/i, '')
    .trim();
}
