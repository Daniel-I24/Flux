import { NUMBER_TOKEN, toNumber } from './spanishNumbers';

export interface TimeExpression {
  hours: number;
  minutes: number;
  match: string;
}

const PERIOD = '(a\\.?\\s?m\\.?|p\\.?\\s?m\\.?|de la manana|de la tarde|de la noche|de la madrugada)';
const MINUTE_WORDS: Record<string, number> = { 'y media': 30, 'y cuarto': 15, 'menos cuarto': -15 };

const CLOCK_PATTERN = new RegExp(
  `\\ba\\s+las?\\s+${NUMBER_TOKEN}(?::(\\d{2}))?(\\s+y media|\\s+y cuarto|\\s+menos cuarto)?(?:\\s*${PERIOD})?`
);

const isAfternoon = (period: string) => /p\.?\s?m|tarde|noche/.test(period);
const isMorning = (period: string) => /a\.?\s?m|manana|madrugada/.test(period);

const to24Hours = (hour: number, period: string): number => {
  if (isAfternoon(period) && hour < 12) return hour + 12;
  if (isMorning(period) && hour === 12) return 0;
  return hour;
};

/** "a las 8", "a las 8:30 pm", "a las ocho y media de la noche", "al mediodía", "a medianoche". */
export const parseTimeExpression = (text: string): TimeExpression | null => {
  if (/\bal? mediodia\b/.test(text)) return { hours: 12, minutes: 0, match: 'mediodia' };
  if (/\ba (la )?medianoche\b/.test(text)) return { hours: 0, minutes: 0, match: 'medianoche' };

  const found = CLOCK_PATTERN.exec(text);
  if (!found) return null;
  const rawHour = toNumber(found[1]);
  if (rawHour === null || rawHour > 23) return null;

  const explicitMinutes = found[2] ? Number.parseInt(found[2], 10) : 0;
  const wordMinutes = MINUTE_WORDS[(found[3] ?? '').trim()] ?? 0;
  const period = found[4] ?? '';
  let hours = to24Hours(rawHour, period);
  let minutes = explicitMinutes + wordMinutes;
  if (minutes < 0) {
    minutes += 60;
    hours = (hours + 23) % 24;
  }
  if (minutes > 59) return null;
  return { hours, minutes, match: found[0].trim() };
};
