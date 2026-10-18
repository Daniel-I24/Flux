// eslint-disable-next-line no-control-regex
const CONTROL_CHARS = /[\u0000-\u0008\u000B\u000C\u000E-\u001F\u007F]/g;
const INVISIBLE_CHARS = /[\u200B-\u200D\u2060\uFEFF]/g;
const MARKUP_TAGS = /<\/?[a-z][^>]*>/gi;
const WHITESPACE = /\s+/g;
const ELLIPSIS = '…';

/**
 * Normalizes any user-provided text before it is stored, classified or synced:
 * removes control/invisible characters and markup tags, collapses whitespace and caps length.
 * Rendering still uses textContent; this is defense in depth.
 */
export const sanitizeUserText = (input: unknown, maxLength = 500): string => {
  if (typeof input !== 'string') return '';
  return input
    .normalize('NFC')
    .replace(CONTROL_CHARS, ' ')
    .replace(INVISIBLE_CHARS, '')
    .replace(MARKUP_TAGS, '')
    .replace(WHITESPACE, ' ')
    .trim()
    .slice(0, maxLength);
};

export const truncate = (text: string, maxLength: number): string =>
  text.length <= maxLength ? text : `${text.slice(0, maxLength - 1).trimEnd()}${ELLIPSIS}`;

export const capitalize = (text: string): string =>
  text.length === 0 ? text : `${text.charAt(0).toLocaleUpperCase()}${text.slice(1)}`;

/** Accent-insensitive lowercase form used for search and keyword matching. */
export const normalizeForSearch = (text: string): string =>
  text
    .toLocaleLowerCase()
    .normalize('NFD')
    .replace(/[̀-ͯ]/g, '');
