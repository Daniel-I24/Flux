import { z } from 'zod';
import { NOTE_CATEGORIES } from './classification.js';

const isoDate = z.string().datetime({ offset: true });

export const RoomCodeSchema = z.string().regex(/^[A-Z0-9]{6}$/);

export const JoinRoomSchema = z.object({
  room: RoomCodeSchema,
  role: z.enum(['watch', 'phone']),
  deviceId: z.string().min(1).max(64),
});

export const NoteSchema = z.object({
  id: z.string().min(1).max(64),
  title: z.string().min(1).max(120),
  text: z.string().min(1).max(500),
  category: z.enum(NOTE_CATEGORIES),
  dueAt: isoDate.nullable(),
  completed: z.boolean(),
  source: z.enum(['watch', 'phone']),
  engine: z.enum(['remote', 'local']),
  createdAt: isoDate,
  updatedAt: isoDate,
  deletedAt: isoDate.nullable(),
});

/** Every relayed message is validated: unknown shapes never reach other devices. */
export const SyncEnvelopeSchema = z.object({
  id: z.string().min(1).max(64),
  origin: z.string().min(1).max(64),
  sentAt: isoDate,
  kind: z.literal('note.upsert'),
  payload: NoteSchema,
});

export type SyncEnvelope = z.infer<typeof SyncEnvelopeSchema>;
