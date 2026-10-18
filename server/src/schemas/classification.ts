import { z } from 'zod';

export const NOTE_CATEGORIES = ['task', 'idea', 'reminder', 'info'] as const;

export const ClassifyRequestSchema = z.object({
  text: z.string().trim().min(1).max(500),
  now: z.string().datetime({ offset: true }),
  timeZone: z.string().min(1).max(64),
});

export type ClassifyRequest = z.infer<typeof ClassifyRequestSchema>;

/** Must match the client's ClassificationSchema: the model answer is validated before leaving the server. */
export const ClassificationSchema = z.object({
  category: z.enum(NOTE_CATEGORIES),
  title: z.string().trim().min(1).max(80),
  text: z.string().trim().min(1).max(500),
  dueAt: z.string().datetime({ offset: true }).nullable(),
  confidence: z.number().min(0).max(1),
});

export type Classification = z.infer<typeof ClassificationSchema>;
