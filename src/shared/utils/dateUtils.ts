/**
 * Date calculation and localization utilities.
 */

export function formatDateTime(isoString: string, locale = 'es-ES'): string {
  const date = new Date(isoString);
  if (isNaN(date.getTime())) return '';

  return date.toLocaleString(locale, {
    weekday: 'short',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function formatTimeOnly(date: Date, locale = 'es-ES'): string {
  return date.toLocaleTimeString(locale, { hour: '2-digit', minute: '2-digit' });
}

export function isPastDate(isoString: string): boolean {
  const date = new Date(isoString);
  return date.getTime() <= Date.now();
}

export function addMinutes(baseDate: Date, minutes: number): Date {
  return new Date(baseDate.getTime() + minutes * 60 * 1000);
}
