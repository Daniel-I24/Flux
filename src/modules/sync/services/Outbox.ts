import { z } from 'zod';
import { logger } from '../../../core/logger/logger';
import { readValidatedJson, writeJson, type KeyValueStore } from '../../../shared/storage/KeyValueStore';
import { SyncEnvelopeSchema, type SyncEnvelope } from '../models/syncEnvelope';

const OutboxEntrySchema = z.object({ key: z.string(), envelope: SyncEnvelopeSchema });
const OutboxSchema = z.array(OutboxEntrySchema);
type OutboxEntry = z.infer<typeof OutboxEntrySchema>;

/**
 * Persistent FIFO queue of envelopes not yet delivered. Entries with the same
 * key (same note) are coalesced so only the latest revision is sent.
 */
export class Outbox {
  constructor(
    private readonly store: KeyValueStore,
    private readonly storageKey: string
  ) {}

  enqueue(key: string, envelope: SyncEnvelope): void {
    const entries = this.read().filter((entry) => entry.key !== key);
    this.write([...entries, { key, envelope }]);
  }

  peekAll(): SyncEnvelope[] {
    return this.read().map((entry) => entry.envelope);
  }

  remove(envelopeId: string): void {
    this.write(this.read().filter((entry) => entry.envelope.id !== envelopeId));
  }

  get size(): number {
    return this.read().length;
  }

  clear(): void {
    this.store.remove(this.storageKey);
  }

  private read(): OutboxEntry[] {
    return readValidatedJson(this.store, this.storageKey, OutboxSchema) ?? [];
  }

  private write(entries: OutboxEntry[]): void {
    try {
      writeJson(this.store, this.storageKey, entries);
    } catch (error) {
      logger.error('Outbox could not be persisted', error);
    }
  }
}
