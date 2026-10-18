import type { Unsubscribe } from '../../../../core/types/domain';
import type { SyncEnvelope, TransportState } from '../../models/syncEnvelope';

/**
 * Adapter contract hiding how bytes travel (BroadcastChannel, Socket.IO...).
 * `send` resolves `true` only when at least one peer received the envelope,
 * so the SyncService knows whether to keep it in the offline queue.
 */
export interface ISyncTransport {
  readonly name: string;
  readonly state: TransportState;
  connect(): void;
  disconnect(): void;
  send(envelope: SyncEnvelope): Promise<boolean>;
  onMessage(handler: (data: unknown) => void): Unsubscribe;
  onStateChange(handler: (state: TransportState) => void): Unsubscribe;
}
