import { z } from 'zod';

const csv = z
  .string()
  .transform((value) => value.split(',').map((item) => item.trim()).filter(Boolean));

const EnvSchema = z.object({
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  PORT: z.coerce.number().int().positive().default(3001),
  CORS_ORIGINS: csv.default('http://localhost:5173,https://localhost:5173'),
  GEMINI_API_KEY: z.string().trim().default(''),
  GEMINI_MODEL: z.string().trim().min(1).default('gemini-2.5-flash'),
  GEMINI_TIMEOUT_MS: z.coerce.number().int().positive().default(6000),
  RATE_LIMIT_WINDOW_MS: z.coerce.number().int().positive().default(60_000),
  RATE_LIMIT_MAX: z.coerce.number().int().positive().default(60),
  BODY_LIMIT: z.string().default('10kb'),
});

export interface ServerConfig {
  env: 'development' | 'production' | 'test';
  port: number;
  corsOrigins: string[];
  gemini: { apiKey: string; model: string; timeoutMs: number };
  rateLimit: { windowMs: number; max: number };
  bodyLimit: string;
  socket: { messagesPerMinute: number; maxPayloadBytes: number };
}

/** Validates process.env once at startup; a bad value stops the server with a clear message. */
export const loadConfig = (source: NodeJS.ProcessEnv = process.env): ServerConfig => {
  const env = EnvSchema.parse(source);
  return {
    env: env.NODE_ENV,
    port: env.PORT,
    corsOrigins: env.CORS_ORIGINS,
    gemini: { apiKey: env.GEMINI_API_KEY, model: env.GEMINI_MODEL, timeoutMs: env.GEMINI_TIMEOUT_MS },
    rateLimit: { windowMs: env.RATE_LIMIT_WINDOW_MS, max: env.RATE_LIMIT_MAX },
    bodyLimit: env.BODY_LIMIT,
    socket: { messagesPerMinute: 120, maxPayloadBytes: 16_384 },
  };
};
