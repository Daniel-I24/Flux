import { z } from 'zod';

const time = z.string().regex(/^\d{2}:\d{2}$/);

export const ClassifyRequestSchema = z.object({
  text: z.string().trim().min(1).max(500),
  now: z.string().datetime({ offset: true }),
  timeZone: z.string().min(1).max(64),
  categories: z
    .array(z.object({ id: z.string().min(1).max(64), name: z.string().trim().min(1).max(30).optional() }))
    .min(1)
    .max(40),
  profile: z
    .object({
      occupation: z.enum(['STUDENT', 'WORKER', 'BOTH', 'OTHER']),
      wakeUp: time,
      sleep: time,
      breakfast: time,
      lunch: time,
      dinner: time,
      activityEndToday: time.optional(),
    })
    .optional(),
});

export type ClassifyRequest = z.infer<typeof ClassifyRequestSchema>;

/** Answer returned to the app; validated again before leaving the server. */
export const ClassificationSchema = z.object({
  categoryId: z.string().min(1).max(64),
  title: z.string().trim().min(1).max(80),
  dueAt: z.string().datetime({ offset: true }).nullable(),
  priority: z.enum(['LOW', 'NORMAL', 'HIGH']),
  requiresAlarm: z.boolean(),
  confidence: z.number().min(0).max(1),
  /** Other categories that fit just as well: the app asks the user (older apps ignore it). */
  alternativeCategoryIds: z.array(z.string().min(1).max(64)).max(3).optional(),
});

export type Classification = z.infer<typeof ClassificationSchema>;

/** Alternatives the app did not send, or equal to the chosen category, are dropped (not worth failing the answer). */
export const withValidAlternatives = (classification: Classification, request: ClassifyRequest): Classification => {
  const { alternativeCategoryIds, ...rest } = classification;
  const valid = (alternativeCategoryIds ?? []).filter(
    (id) => id !== classification.categoryId && request.categories.some((category) => category.id === id)
  );
  return valid.length > 0 ? { ...rest, alternativeCategoryIds: valid } : rest;
};

/** The model may only pick one of the categories the app sent. */
export const belongsToRequest = (classification: Classification, request: ClassifyRequest): boolean =>
  request.categories.some((category) => category.id === classification.categoryId);
