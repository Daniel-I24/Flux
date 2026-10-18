import { z } from 'zod';
import { NOTE_CATEGORIES, type AiEngine } from '../../../core/types/domain';

export const MAX_TITLE_LENGTH = 80;
export const MAX_TEXT_LENGTH = 500;

/** Shape both strategies must return; also validates the server/Gemini response. */
export const ClassificationSchema = z.object({
  category: z.enum(NOTE_CATEGORIES),
  title: z.string().trim().min(1).max(MAX_TITLE_LENGTH),
  text: z.string().trim().min(1).max(MAX_TEXT_LENGTH),
  dueAt: z.string().datetime({ offset: true }).nullable(),
  confidence: z.number().min(0).max(1),
});

export type Classification = z.infer<typeof ClassificationSchema>;

export interface ClassificationResult extends Classification {
  engine: AiEngine;
}

export interface ClassifyRequest {
  text: string;
  now: Date;
  timeZone: string;
}
