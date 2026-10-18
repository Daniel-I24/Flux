import { describe, expect, it, vi } from 'vitest';
import { LocalSyncTransport } from '../services/transports/LocalSyncTransport';
import { createChannelHub } from './fakes';

const sampleEnvelope = {
  id: 'm1',
  origin: 'watch',
  sentAt: new Date().toISOString(),
  kind: 'note.upsert' as const,
  payload: { id: 'n1' },
};

describe('LocalSyncTransport', () => {
  it('discovers peers through the presence handshake', () => {
    const hub = createChannelHub();
    const watch = new LocalSyncTransport('room', 'watch', hub);
    const phone = new LocalSyncTransport('room', 'phone', hub);

    watch.connect();
    expect(watch.state.peers).toBe(0);
    phone.connect();

    expect(watch.state).toEqual({ status: 'connected', peers: 1 });
    expect(phone.state.peers).toBe(1);
  });

  it('reports no delivery without peers and delivers when a peer exists', async () => {
    const hub = createChannelHub();
    const watch = new LocalSyncTransport('room', 'watch', hub);
    watch.connect();
    await expect(watch.send(sampleEnvelope)).resolves.toBe(false);

    const phone = new LocalSyncTransport('room', 'phone', hub);
    const received = vi.fn();
    phone.onMessage(received);
    phone.connect();

    await expect(watch.send(sampleEnvelope)).resolves.toBe(true);
    expect(received).toHaveBeenCalledWith(sampleEnvelope);
  });

  it('isolates rooms and forgets peers that leave', () => {
    const hub = createChannelHub();
    const watch = new LocalSyncTransport('room-a', 'watch', hub);
    const stranger = new LocalSyncTransport('room-b', 'phone', hub);
    const phone = new LocalSyncTransport('room-a', 'phone', hub);
    watch.connect();
    stranger.connect();
    phone.connect();
    expect(watch.state.peers).toBe(1);

    phone.disconnect();

    expect(watch.state.peers).toBe(0);
    expect(phone.state.status).toBe('offline');
  });

  it('ignores frames that are not valid', () => {
    const hub = createChannelHub();
    const watch = new LocalSyncTransport('room', 'watch', hub);
    const received = vi.fn();
    watch.onMessage(received);
    watch.connect();

    hub('room').postMessage({ type: 'envelope' });

    expect(received).not.toHaveBeenCalled();
  });
});
