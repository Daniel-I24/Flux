import { describe, expect, it, vi } from 'vitest';
import { MemoryStorage } from '../../../shared/storage/KeyValueStore';
import { PairingService } from '../services/PairingService';
import { CompositeSyncTransport } from '../services/transports/CompositeSyncTransport';
import { FakeTransport } from './fakes';

const envelope = { id: 'm1', origin: 'w', sentAt: new Date().toISOString(), kind: 'note.upsert' as const, payload: {} };

describe('CompositeSyncTransport', () => {
  it('follows the primary transport status while nobody is reachable', () => {
    const socket = new FakeTransport();
    const local = new FakeTransport();
    const composite = new CompositeSyncTransport([socket, local]);
    composite.connect();
    socket.goOffline();
    expect(composite.state).toEqual({ status: 'reconnecting', peers: 0 });

    local.goOnline(1);
    expect(composite.state).toEqual({ status: 'connected', peers: 1 });
  });

  it('delivers when any child delivers and forwards messages from all children', async () => {
    const socket = new FakeTransport();
    const local = new FakeTransport();
    const composite = new CompositeSyncTransport([socket, local]);
    const received = vi.fn();
    composite.onMessage(received);
    composite.connect();
    socket.deliver = false;

    await expect(composite.send(envelope)).resolves.toBe(true);
    local.receive('hello');
    expect(received).toHaveBeenCalledWith('hello');
  });
});

describe('PairingService', () => {
  it('creates and keeps a 6-character code', () => {
    const service = new PairingService(new MemoryStorage(), 'pair', 6);
    const code = service.getOrCreateCode();
    expect(code).toMatch(/^[A-Z0-9]{6}$/);
    expect(service.getOrCreateCode()).toBe(code);
    expect(service.regenerate()).not.toBe('');
  });

  it('normalizes typed codes and rejects invalid ones', () => {
    const service = new PairingService(new MemoryStorage(), 'pair', 6);
    expect(service.setCode(' abc-12 3 ')).toBe('ABC123');
    expect(() => service.setCode('abc')).toThrow();
    service.clear();
    expect(service.getCode()).toBeNull();
  });

  it('builds the QR URL', () => {
    expect(PairingService.buildPairingUrl('https://192.168.1.5:5173/', 'ABC123')).toBe('https://192.168.1.5:5173/phone?pair=ABC123');
  });
});
