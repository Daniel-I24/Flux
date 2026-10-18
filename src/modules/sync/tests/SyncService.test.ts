import { describe, expect, it, vi } from 'vitest';
import { z } from 'zod';
import { MemoryStorage } from '../../../shared/storage/KeyValueStore';
import { Outbox } from '../services/Outbox';
import { SyncService } from '../services/SyncService';
import { FakeTransport } from './fakes';

const PayloadSchema = z.object({ id: z.string(), rev: z.number() });
type Payload = z.infer<typeof PayloadSchema>;

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));

const setup = (deviceId = 'watch-1') => {
  const transport = new FakeTransport();
  const outbox = new Outbox(new MemoryStorage(), 'outbox');
  const onActivity = vi.fn();
  const service = new SyncService<Payload>({
    transport,
    outbox,
    deviceId,
    payloadSchema: PayloadSchema,
    keyOf: (payload) => payload.id,
    seenLimit: 3,
    onActivity,
  });
  return { transport, outbox, service, onActivity };
};

const envelope = (id: string, payload: unknown, origin = 'phone-1') => ({
  id,
  origin,
  sentAt: new Date().toISOString(),
  kind: 'note.upsert',
  payload,
});

describe('SyncService', () => {
  it('delivers immediately when connected and clears the queue', async () => {
    const { transport, outbox, service, onActivity } = setup();
    service.start();

    service.publish({ id: 'n1', rev: 1 });
    await flush();

    expect(transport.sent).toHaveLength(1);
    expect(outbox.size).toBe(0);
    expect(onActivity).toHaveBeenCalledWith('out');
  });

  it('queues while offline and flushes in order after reconnecting', async () => {
    const { transport, outbox, service } = setup();
    service.start();
    transport.goOffline();

    service.publish({ id: 'n1', rev: 1 });
    service.publish({ id: 'n2', rev: 1 });
    await flush();
    expect(transport.sent).toHaveLength(0);
    expect(service.state.pending).toBe(2);

    transport.goOnline();
    await flush();

    expect(transport.sent.map((sent) => (sent.payload as Payload).id)).toEqual(['n1', 'n2']);
    expect(outbox.size).toBe(0);
  });

  it('coalesces queued revisions of the same item', async () => {
    const { transport, service } = setup();
    service.start();
    transport.goOffline();

    service.publish({ id: 'n1', rev: 1 });
    service.publish({ id: 'n1', rev: 2 });
    transport.goOnline();
    await flush();

    expect(transport.sent).toHaveLength(1);
    expect(transport.sent[0]?.payload).toEqual({ id: 'n1', rev: 2 });
  });

  it('keeps the queue when the peer does not acknowledge delivery', async () => {
    const { transport, outbox, service } = setup();
    service.start();
    transport.deliver = false;

    service.publish({ id: 'n1', rev: 1 });
    await flush();

    expect(outbox.size).toBe(1);
  });

  it('survives a restart thanks to the persistent outbox', async () => {
    const storage = new MemoryStorage();
    const offline = new FakeTransport();
    const first = new SyncService<Payload>({
      transport: offline, outbox: new Outbox(storage, 'outbox'), deviceId: 'w', payloadSchema: PayloadSchema, keyOf: (p) => p.id, seenLimit: 10,
    });
    first.publish({ id: 'n1', rev: 1 });

    const online = new FakeTransport();
    const second = new SyncService<Payload>({
      transport: online, outbox: new Outbox(storage, 'outbox'), deviceId: 'w', payloadSchema: PayloadSchema, keyOf: (p) => p.id, seenLimit: 10,
    });
    second.start();
    await flush();

    expect(online.sent).toHaveLength(1);
  });

  it('delivers each incoming envelope once and ignores its own messages', () => {
    const { transport, service, onActivity } = setup();
    const handler = vi.fn();
    service.onPayload(handler);
    service.start();

    transport.receive(envelope('m1', { id: 'n1', rev: 1 }));
    transport.receive(envelope('m1', { id: 'n1', rev: 1 }));
    transport.receive(envelope('m2', { id: 'n2', rev: 1 }, 'watch-1'));

    expect(handler).toHaveBeenCalledTimes(1);
    expect(onActivity).toHaveBeenCalledWith('in');
  });

  it('rejects malformed envelopes and payloads', () => {
    const { transport, service } = setup();
    const handler = vi.fn();
    service.onPayload(handler);
    service.start();

    transport.receive({ hello: 'world' });
    transport.receive(envelope('m3', { id: 42 }));

    expect(handler).not.toHaveBeenCalled();
  });

  it('bounds the de-duplication memory', () => {
    const { transport, service } = setup();
    const handler = vi.fn();
    service.onPayload(handler);
    service.start();

    ['a', 'b', 'c', 'd'].forEach((id) => transport.receive(envelope(id, { id, rev: 1 })));
    transport.receive(envelope('a', { id: 'a', rev: 1 }));

    expect(handler).toHaveBeenCalledTimes(5);
  });
});
