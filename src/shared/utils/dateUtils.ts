export const SECOND_MS = 1000;
export const MINUTE_MS = 60 * SECOND_MS;
export const HOUR_MS = 60 * MINUTE_MS;
export const DAY_MS = 24 * HOUR_MS;

export const addMilliseconds = (date: Date, ms: number): Date => new Date(date.getTime() + ms);
export const addMinutes = (date: Date, minutes: number): Date =>
  addMilliseconds(date, minutes * MINUTE_MS);

export const isValidDate = (value: Date): boolean => !Number.isNaN(value.getTime());

export const parseIsoDate = (iso: string | null | undefined): Date | null => {
  if (!iso) return null;
  const date = new Date(iso);
  return isValidDate(date) ? date : null;
};

type RelativeUnit = readonly [Intl.RelativeTimeFormatUnit, number];

const SECOND_UNIT: RelativeUnit = ['second', SECOND_MS];
const RELATIVE_UNITS: readonly RelativeUnit[] = [
  ['day', DAY_MS],
  ['hour', HOUR_MS],
  ['minute', MINUTE_MS],
  SECOND_UNIT,
];

/** "dentro de 5 minutos", "hace 2 horas", "mañana"... using the platform Intl API. */
export const formatRelativeTime = (date: Date, now: Date, locale: string): string => {
  const diff = date.getTime() - now.getTime();
  const formatter = new Intl.RelativeTimeFormat(locale, { numeric: 'auto' });
  const [unit, size] = RELATIVE_UNITS.find(([, unitMs]) => Math.abs(diff) >= unitMs) ?? SECOND_UNIT;
  return formatter.format(Math.round(diff / size), unit);
};

export const formatDateTime = (date: Date, locale: string): string =>
  date.toLocaleString(locale, {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });

export const formatClock = (date: Date, locale: string): string =>
  date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });

export const formatShortDate = (date: Date, locale: string): string =>
  date.toLocaleDateString(locale, { weekday: 'short', day: 'numeric', month: 'short' });
