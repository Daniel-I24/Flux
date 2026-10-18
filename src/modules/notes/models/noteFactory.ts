import type { DeviceRole } from '../../../core/types/domain';
import { createId } from '../../../shared/utils/id';
import type { ClassificationResult } from '../../ai/models/classification';
import type { Note } from './note';

/** Pure factory shared by the watch (which has no repository) and the phone. */
export const createNoteFromClassification = (
  classification: ClassificationResult,
  source: DeviceRole,
  now: Date = new Date()
): Note => {
  const timestamp = now.toISOString();
  return {
    id: createId(),
    title: classification.title,
    text: classification.text,
    category: classification.category,
    dueAt: classification.dueAt,
    completed: false,
    source,
    engine: classification.engine,
    createdAt: timestamp,
    updatedAt: timestamp,
    deletedAt: null,
  };
};
