import { z } from 'zod';
import type { ConnectionStatus } from '../../../core/types/domain';

/** Wire format shared by every transport (and validated again by the server). */
export const SyncEnvelopeSchema = z.object({
  id: z.string().min(1).max(64),
  origin: z.string().min(1).max(64),
  sentAt: z.string().datetime({ offset: true }),
  kind: z.literal('note.upsert'),
  payload: z.unknown(),
});

export type SyncEnvelope = z.infer<typeof SyncEnvelopeSchema>;

export interface TransportState {
  status: ConnectionStatus;
  /** Other devices currently reachable in the same room. */
  peers: number;
}

export interface SyncState extends TransportState {
  /** Messages waiting in the offline queue. */
  pending: number;
}

export const OFFLINE_STATE: TransportState = { status: 'offline', peers: 0 };
