import { z } from 'zod';

/** GET /api/health response. */
export const HealthResponseSchema = z.object({
  status: z.literal('ok'),
  ai: z.object({
    gemini: z.boolean(),
    model: z.string().nullable(),
  }),
});

export type HealthResponse = z.infer<typeof HealthResponseSchema>;

export const API_PATHS = {
  health: '/api/health',
  classify: '/api/classify',
} as const;
