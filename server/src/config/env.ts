import { z } from 'zod';

const csv = z
  .string()
  .transform((value) => value.split(',').map((item) => item.trim()).filter(Boolean));

const flag = z
  .string()
  .optional()
  .transform((value) => value === 'true' || value === '1');

const EnvSchema = z.object({
  NODE_ENV: z.enum(['development', 'production', 'test']).default('development'),
  PORT: z.coerce.number().int().positive().default(3001),
  CORS_ORIGINS: csv.default(''),
  GEMINI_API_KEY: z.string().trim().default(''),
  GEMINI_MODEL: z.string().trim().min(1).default('gemini-3.5-flash-lite'),
  GEMINI_TIMEOUT_MS: z.coerce.number().int().positive().default(7500),
  FIREBASE_PROJECT_ID: z.string().trim().default(''),
  FIREBASE_SERVICE_ACCOUNT_BASE64: z.string().trim().default(''),
  APP_CHECK_REQUIRED: flag,
  RATE_LIMIT_WINDOW_MS: z.coerce.number().int().positive().default(60_000),
  RATE_LIMIT_PER_USER: z.coerce.number().int().positive().default(30),
  RATE_LIMIT_PER_IP: z.coerce.number().int().positive().default(120),
  BODY_LIMIT: z.string().default('16kb'),
});

export interface ServerConfig {
  env: 'development' | 'production' | 'test';
  port: number;
  /** Browser origins allowed by CORS. The Android apps send no Origin header. */
  corsOrigins: string[];
  gemini: { apiKey: string; model: string; timeoutMs: number };
  firebase: { projectId: string; serviceAccountBase64: string; appCheckRequired: boolean };
  rateLimit: { windowMs: number; perUser: number; perIp: number };
  bodyLimit: string;
}

/** Validates process.env once at startup; a bad value stops the server with a clear message. */
export const loadConfig = (source: NodeJS.ProcessEnv = process.env): ServerConfig => {
  const env = EnvSchema.parse(source);
  return {
    env: env.NODE_ENV,
    port: env.PORT,
    corsOrigins: env.CORS_ORIGINS,
    gemini: { apiKey: env.GEMINI_API_KEY, model: env.GEMINI_MODEL, timeoutMs: env.GEMINI_TIMEOUT_MS },
    firebase: {
      projectId: env.FIREBASE_PROJECT_ID,
      serviceAccountBase64: env.FIREBASE_SERVICE_ACCOUNT_BASE64,
      appCheckRequired: env.APP_CHECK_REQUIRED,
    },
    rateLimit: { windowMs: env.RATE_LIMIT_WINDOW_MS, perUser: env.RATE_LIMIT_PER_USER, perIp: env.RATE_LIMIT_PER_IP },
    bodyLimit: env.BODY_LIMIT,
  };
};
