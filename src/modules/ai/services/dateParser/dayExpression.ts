const WEEKDAYS: Record<string, number> = {
  domingo: 0,
  lunes: 1,
  martes: 2,
  miercoles: 3,
  jueves: 4,
  viernes: 5,
  sabado: 6,
};

/** Default hour when only a part of the day is mentioned. */
const DAY_PART_HOURS: Record<string, number> = {
  manana: 9,
  tarde: 16,
  noche: 20,
};

export interface DayExpression {
  /** Calendar day (time set to 00:00) the phrase refers to. */
  day: Date;
  /** Hour implied by "esta tarde" / "esta noche" when no explicit time is given. */
  impliedHour: number | null;
  match: string;
}

const startOfDay = (date: Date, addDays = 0): Date => {
  const copy = new Date(date);
  copy.setHours(0, 0, 0, 0);
  copy.setDate(copy.getDate() + addDays);
  return copy;
};

const WEEKDAY_PATTERN = new RegExp(
  `\\b(?:el|este|esta|para el|el proximo|el siguiente)?\\s*(${Object.keys(WEEKDAYS).join('|')})(?:\\s+(proximo|que viene))?\\b`
);

const parseWeekday = (text: string, now: Date): DayExpression | null => {
  const found = WEEKDAY_PATTERN.exec(text);
  if (!found) return null;
  const target = WEEKDAYS[found[1] ?? ''];
  if (target === undefined) return null;
  const explicitNext = /proximo|siguiente|que viene/.test(found[0]);
  let delta = (target - now.getDay() + 7) % 7;
  if (delta === 0 && explicitNext) delta = 7;
  return { day: startOfDay(now, delta), impliedHour: null, match: found[0].trim() };
};

/**
 * Detects the day: "hoy", "mañana", "pasado mañana", weekdays and "esta tarde/noche".
 * Expects accent-free lowercase text. "de la mañana" (a.m.) is not treated as tomorrow.
 */
export const parseDayExpression = (text: string, now: Date): DayExpression | null => {
  const withoutAm = text.replace(/\b(de|por) la manana\b/g, ' ');

  if (/\bpasado manana\b/.test(withoutAm)) {
    return { day: startOfDay(now, 2), impliedHour: null, match: 'pasado manana' };
  }
  const tomorrow = /\bmanana(?:\s+(?:en la|por la)\s+(tarde|noche))?\b/.exec(withoutAm);
  if (tomorrow) {
    const part = tomorrow[1];
    return { day: startOfDay(now, 1), impliedHour: part ? DAY_PART_HOURS[part] ?? null : null, match: tomorrow[0] };
  }
  const tonight = /\b(?:esta|en la|por la)\s+(tarde|noche)\b/.exec(withoutAm);
  if (tonight) {
    return { day: startOfDay(now), impliedHour: DAY_PART_HOURS[tonight[1] ?? ''] ?? null, match: tonight[0] };
  }
  if (/\bhoy\b/.test(withoutAm)) return { day: startOfDay(now), impliedHour: null, match: 'hoy' };
  return parseWeekday(withoutAm, now);
};
