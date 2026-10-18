import { describe, expect, it } from 'vitest';
import { parseSpanishDate } from '../services/dateParser';

// Sunday 18 Oct 2026, 10:00 local time.
const NOW = new Date(2026, 9, 18, 10, 0, 0);

const parse = (text: string) => parseSpanishDate(text, NOW)?.date ?? null;
const local = (month: number, day: number, hours: number, minutes = 0) =>
  new Date(2026, month, day, hours, minutes, 0, 0).getTime();

describe('parseSpanishDate', () => {
  it('returns null when there is no date', () => {
    expect(parse('El código del laboratorio es 4829')).toBeNull();
  });

  it('parses relative offsets in seconds, minutes and hours', () => {
    expect(parse('recuérdame tomar agua en 10 segundos')?.getTime()).toBe(NOW.getTime() + 10_000);
    expect(parse('en cinco minutos llamar a Ana')?.getTime()).toBe(NOW.getTime() + 5 * 60_000);
    expect(parse('dentro de 2 horas')?.getTime()).toBe(NOW.getTime() + 2 * 3_600_000);
    expect(parse('en media hora')?.getTime()).toBe(NOW.getTime() + 30 * 60_000);
  });

  it('parses "mañana a las 8 am"', () => {
    expect(parse('entregar el proyecto mañana a las 8 am')?.getTime()).toBe(local(9, 19, 8));
  });

  it('does not confuse "de la mañana" with tomorrow', () => {
    expect(parse('a las 11 de la mañana')?.getTime()).toBe(local(9, 18, 11));
  });

  it('moves a past time without a day to tomorrow', () => {
    expect(parse('a las 7 de la mañana')?.getTime()).toBe(local(9, 19, 7));
  });

  it('understands pm, word numbers and "y media"', () => {
    expect(parse('hoy a las 3 pm')?.getTime()).toBe(local(9, 18, 15));
    expect(parse('a las ocho y media de la noche')?.getTime()).toBe(local(9, 18, 20, 30));
    expect(parse('mañana a la una de la tarde')?.getTime()).toBe(local(9, 19, 13));
    expect(parse('a las 15:45')?.getTime()).toBe(local(9, 18, 15, 45));
  });

  it('parses weekdays to the next occurrence', () => {
    expect(parse('el viernes')?.getTime()).toBe(local(9, 23, 9));
    expect(parse('el lunes a las 10')?.getTime()).toBe(local(9, 19, 10));
    expect(parse('el próximo sábado')?.getTime()).toBe(local(9, 24, 9));
    expect(parse('el próximo domingo')?.getTime()).toBe(local(9, 25, 9));
  });

  it('parses "pasado mañana" and parts of the day', () => {
    expect(parse('pasado mañana')?.getTime()).toBe(local(9, 20, 9));
    expect(parse('esta noche')?.getTime()).toBe(local(9, 18, 20));
    expect(parse('mañana en la tarde')?.getTime()).toBe(local(9, 19, 16));
    expect(parse('al mediodía')?.getTime()).toBe(local(9, 18, 12));
  });

  it('reports the matched fragments', () => {
    expect(parseSpanishDate('mañana a las 8 am', NOW)?.matches).toEqual(['manana', 'a las 8 am']);
  });
});
