import type { ZodType } from 'zod';
import { EventBus } from '../../../core/events/EventBus';
import { logger } from '../../../core/logger/logger';
import type { Unsubscribe } from '../../../core/types/domain';
import { createId } from '../../../shared/utils/id';
import { SyncEnvelopeSchema, type SyncEnvelope, type SyncState } from '../models/syncEnvelope';
import type { Outbox } from './Outbox';
import type { ISyncTransport } from './transports/ISyncTransport';

export interface SyncServiceOptions<T> {
  transport: ISyncTransport;
  outbox: Outbox;
  deviceId: string;
  payloadSchema: ZodType<T>;
  /** Key used to coalesce queued messages (e.g. the note id). */
  keyOf: (payload: T) => string;
  seenLimit: number;
  onActivity?: (direction: 'in' | 'out') => void;
  now?: () => Date;
}

interface SyncEvents<T> {
  payload: T;
  state: SyncState;
}

/**
 * Transport-agnostic sync: every outgoing payload goes through a persistent
 * outbox (offline queue) and is removed only after a peer received it.
 * Incoming envelopes are validated and de-duplicated by id and origin.
 */
export class SyncService<T> {
  private readonly events = new EventBus<SyncEvents<T>>();
  private readonly seen = new Set<string>();
  private readonly subscriptions: Unsubscribe[] = [];
  private flushing: Promise<void> | null = null;
  private rerunRequested = false;

  constructor(private readonly options: SyncServiceOptions<T>) {}

  get state(): SyncState {
    return { ...this.options.transport.state, pending: this.options.outbox.size };
  }

  start(): void {
    const { transport } = this.options;
    this.subscriptions.push(
      transport.onMessage((data) => this.receive(data)),
      transport.onStateChange(() => {
        this.emitState();
        void this.flush();
      })
    );
    transport.connect();
    this.emitState();
    void this.flush();
  }

  stop(): void {
    this.subscriptions.splice(0).forEach((unsubscribe) => unsubscribe());
    this.options.transport.disconnect();
  }

  onPayload(handler: (payload: T) => void): Unsubscribe {
    return this.events.on('payload', handler);
  }

  onState(handler: (state: SyncState) => void): Unsubscribe {
    return this.events.on('state', handler);
  }

  /** Queues a payload and tries to deliver it right away. */
  publish(payload: T): SyncEnvelope {
    const envelope: SyncEnvelope = {
      id: createId(),
      origin: this.options.deviceId,
      sentAt: (this.options.now?.() ?? new Date()).toISOString(),
      kind: 'note.upsert',
      payload,
    };
    this.options.outbox.enqueue(this.options.keyOf(payload), envelope);
    this.emitState();
    void this.flush();
    return envelope;
  }

  isPending(envelopeId: string): boolean {
    return this.options.outbox.peekAll().some((envelope) => envelope.id === envelopeId);
  }

  /** Delivers queued envelopes in order; stops at the first one no peer received. */
  flush(): Promise<void> {
    if (this.flushing) {
      this.rerunRequested = true;
      return this.flushing;
    }
    this.flushing = this.drain().finally(() => {
      this.flushing = null;
      this.emitState();
      if (this.rerunRequested) {
        this.rerunRequested = false;
        void this.flush();
      }
    });
    return this.flushing;
  }

  private async drain(): Promise<void> {
    const { outbox, transport } = this.options;
    for (let next = outbox.peekAll()[0]; next; next = outbox.peekAll()[0]) {
      const delivered = await transport.send(next).catch(() => false);
      if (!delivered) return;
      outbox.remove(next.id);
      this.options.onActivity?.('out');
    }
  }

  private receive(data: unknown): void {
    const envelope = SyncEnvelopeSchema.safeParse(data);
    if (!envelope.success) {
      logger.warn('Discarding malformed sync envelope', envelope.error.issues);
      return;
    }
    const { id, origin, payload } = envelope.data;
    if (origin === this.options.deviceId || this.seen.has(id)) return;
    this.remember(id);
    const parsed = this.options.payloadSchema.safeParse(payload);
    if (!parsed.success) {
      logger.warn('Discarding sync payload with invalid shape', parsed.error.issues);
      return;
    }
    this.options.onActivity?.('in');
    this.events.emit('payload', parsed.data);
  }

  private remember(id: string): void {
    this.seen.add(id);
    if (this.seen.size <= this.options.seenLimit) return;
    const oldest = this.seen.values().next().value;
    if (oldest !== undefined) this.seen.delete(oldest);
  }

  private emitState(): void {
    this.events.emit('state', this.state);
  }
}
