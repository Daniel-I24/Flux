/** Minimal leveled logger (the only server file allowed to use the console). */
const silent = process.env.NODE_ENV === 'test';

const write = (level: 'info' | 'warn' | 'error', message: string, details?: unknown) => {
  if (silent) return;
  const line = `[flux-server] ${new Date().toISOString()} ${level.toUpperCase()} ${message}`;
  if (level === 'error') console.error(line, details ?? '');
  else if (level === 'warn') console.warn(line, details ?? '');
  else console.info(line);
};

export const logger = {
  info: (message: string) => write('info', message),
  warn: (message: string, details?: unknown) => write('warn', message, details),
  error: (message: string, details?: unknown) => write('error', message, details),
};
