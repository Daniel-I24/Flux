import { HOUR_MS, MINUTE_MS, SECOND_MS } from '../../../../shared/utils/dateUtils';
import { NUMBER_TOKEN, toNumber } from './spanishNumbers';

const UNIT_MS: Record<string, number> = {
  segundo: SECOND_MS,
  minuto: MINUTE_MS,
  hora: HOUR_MS,
};

const OFFSET_PATTERN = new RegExp(
  `\\b(?:en|dentro de)\\s+${NUMBER_TOKEN}\\s+(segundo|minuto|hora)s?\\b`
);
const HALF_HOUR_PATTERN = /\b(?:en|dentro de)\s+media\s+hora\b/;

export interface ParsedFragment {
  date: Date;
  match: string;
}

/** "en 10 segundos", "dentro de cinco minutos", "en 2 horas", "en media hora". */
export const parseRelativeOffset = (text: string, now: Date): ParsedFragment | null => {
  const halfHour = HALF_HOUR_PATTERN.exec(text);
  if (halfHour) return { date: new Date(now.getTime() + 30 * MINUTE_MS), match: halfHour[0] };

  const offset = OFFSET_PATTERN.exec(text);
  if (!offset) return null;
  const amount = toNumber(offset[1]);
  const unit = UNIT_MS[offset[2] ?? ''];
  if (amount === null || unit === undefined || amount <= 0) return null;
  return { date: new Date(now.getTime() + amount * unit), match: offset[0] };
};
