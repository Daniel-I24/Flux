import { normalizeForSearch } from '../../../../shared/utils/sanitizer';
import { parseDayExpression } from './dayExpression';
import { parseRelativeOffset } from './relativeOffset';
import { parseTimeExpression } from './timeExpression';

const DEFAULT_HOUR = 9;

export interface ParsedDate {
  date: Date;
  /** Normalized fragments that produced the date (useful to clean titles). */
  matches: string[];
}

/**
 * Deterministic Spanish date extraction:
 * relative offsets ("en 5 minutos") win; otherwise day + time are combined.
 * A time without a day that already passed today moves to tomorrow.
 */
export const parseSpanishDate = (input: string, now: Date = new Date()): ParsedDate | null => {
  const text = normalizeForSearch(input);

  const offset = parseRelativeOffset(text, now);
  if (offset) return { date: offset.date, matches: [offset.match] };

  const day = parseDayExpression(text, now);
  const time = parseTimeExpression(text);
  if (!day && !time) return null;

  const target = new Date(day ? day.day : now);
  const hours = time?.hours ?? day?.impliedHour ?? DEFAULT_HOUR;
  target.setHours(hours, time?.minutes ?? 0, 0, 0);

  if (!day && target.getTime() <= now.getTime()) target.setDate(target.getDate() + 1);

  return {
    date: target,
    matches: [day?.match, time?.match].filter((match): match is string => Boolean(match)),
  };
};
