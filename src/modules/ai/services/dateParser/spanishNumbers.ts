/** Spanish number words that commonly appear in dictated times and offsets (accents removed). */
const NUMBER_WORDS: Record<string, number> = {
  un: 1,
  uno: 1,
  una: 1,
  dos: 2,
  tres: 3,
  cuatro: 4,
  cinco: 5,
  seis: 6,
  siete: 7,
  ocho: 8,
  nueve: 9,
  diez: 10,
  once: 11,
  doce: 12,
  trece: 13,
  catorce: 14,
  quince: 15,
  veinte: 20,
  treinta: 30,
  cuarenta: 40,
  cincuenta: 50,
};

// Longest words first so "una" is not captured as "un".
const WORDS_BY_LENGTH = Object.keys(NUMBER_WORDS).sort((a, b) => b.length - a.length);

export const NUMBER_TOKEN = `(\\d{1,3}|${WORDS_BY_LENGTH.join('|')})`;

export const toNumber = (token: string | undefined): number | null => {
  if (!token) return null;
  if (/^\d+$/.test(token)) return Number.parseInt(token, 10);
  return NUMBER_WORDS[token] ?? null;
};
