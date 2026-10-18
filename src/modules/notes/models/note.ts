import { z } from 'zod';
import { DEVICE_ROLES, NOTE_CATEGORIES } from '../../../core/types/domain';

const isoDate = z.string().datetime({ offset: true });

/**
 * A note. Deletions are soft (`deletedAt` tombstone) so they can be synced and
 * merged with last-write-wins on `updatedAt`.
 */
export const NoteSchema = z.object({
  id: z.string().min(1).max(64),
  title: z.string().min(1).max(120),
  text: z.string().min(1).max(500),
  category: z.enum(NOTE_CATEGORIES),
  dueAt: isoDate.nullable(),
  completed: z.boolean(),
  source: z.enum(DEVICE_ROLES),
  engine: z.enum(['remote', 'local']),
  createdAt: isoDate,
  updatedAt: isoDate,
  deletedAt: isoDate.nullable(),
});

export type Note = z.infer<typeof NoteSchema>;

export const isActive = (note: Note): boolean => note.deletedAt === null;

/** True when `candidate` is a newer revision of the same note. */
export const isNewerRevision = (candidate: Note, current: Note | undefined): boolean =>
  !current || Date.parse(candidate.updatedAt) > Date.parse(current.updatedAt);
