import { EventBus } from '../../../../core/events/EventBus';
import type { Unsubscribe } from '../../../../core/types/domain';
import { OFFLINE_STATE, type SyncEnvelope, type TransportState } from '../../models/syncEnvelope';
import type { ISyncTransport } from './ISyncTransport';

interface TransportEvents {
  message: unknown;
  state: TransportState;
}

/** Shared observer plumbing for transports (DRY). */
export abstract class TransportBase implements ISyncTransport {
  abstract readonly name: string;
  private readonly events = new EventBus<TransportEvents>();
  private current: TransportState = OFFLINE_STATE;

  get state(): TransportState {
    return this.current;
  }

  abstract connect(): void;
  abstract disconnect(): void;
  abstract send(envelope: SyncEnvelope): Promise<boolean>;

  onMessage(handler: (data: unknown) => void): Unsubscribe {
    return this.events.on('message', handler);
  }

  onStateChange(handler: (state: TransportState) => void): Unsubscribe {
    return this.events.on('state', handler);
  }

  protected emitMessage(data: unknown): void {
    this.events.emit('message', data);
  }

  protected setState(patch: Partial<TransportState>): void {
    const next = { ...this.current, ...patch };
    if (next.status === this.current.status && next.peers === this.current.peers) return;
    this.current = next;
    this.events.emit('state', next);
  }
}
