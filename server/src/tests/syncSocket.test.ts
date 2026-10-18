import type { AddressInfo } from 'node:net';
import { io as connect, type Socket } from 'socket.io-client';
import { afterEach, beforeEach, describe, expect, it } from 'vitest';
import { createFluxServer, type FluxServer } from '../server.js';
import { FakeClassifier, testConfig } from './helpers.js';

const NOTE = {
  id: 'n1',
  title: 'Comprar pan',
  text: 'Comprar pan',
  category: 'task',
  dueAt: null,
  completed: false,
  source: 'watch',
  engine: 'local',
  createdAt: '2026-10-18T10:00:00.000Z',
  updatedAt: '2026-10-18T10:00:00.000Z',
  deletedAt: null,
};
const envelope = (id = 'm1') => ({ id, origin: 'watch-1', sentAt: NOTE.createdAt, kind: 'note.upsert', payload: NOTE });

let server: FluxServer;
let url = '';
const clients: Socket[] = [];

const client = async (room: string, role: 'watch' | 'phone'): Promise<Socket> => {
  const socket = connect(url, { transports: ['websocket'], forceNew: true });
  clients.push(socket);
  await socket.emitWithAck('room:join', { room, role, deviceId: `${role}-${clients.length}` });
  return socket;
};

beforeEach(async () => {
  server = createFluxServer(testConfig(), new FakeClassifier());
  await new Promise<void>((resolve) => server.http.listen(0, resolve));
  url = `http://localhost:${(server.http.address() as AddressInfo).port}`;
});

afterEach(async () => {
  clients.splice(0).forEach((socket) => socket.close());
  await server.close();
});

describe('sync socket relay', () => {
  it('relays a valid envelope to the peer and acknowledges delivery', async () => {
    const watch = await client('ABC123', 'watch');
    const phone = await client('ABC123', 'phone');
    const received = new Promise((resolve) => phone.once('sync:message', resolve));

    const ack = await watch.emitWithAck('sync:message', envelope());

    expect(ack).toEqual({ delivered: true });
    await expect(received).resolves.toEqual(envelope());
  });

  it('reports no delivery when alone so the client keeps its offline queue', async () => {
    const watch = await client('ABC123', 'watch');
    await expect(watch.emitWithAck('sync:message', envelope())).resolves.toEqual({ delivered: false });
  });

  it('isolates rooms by pairing code', async () => {
    const watch = await client('ABC123', 'watch');
    await client('ZZZ999', 'phone');
    await expect(watch.emitWithAck('sync:message', envelope())).resolves.toEqual({ delivered: false });
  });

  it('rejects invalid join codes and invalid envelopes', async () => {
    const socket = connect(url, { transports: ['websocket'], forceNew: true });
    clients.push(socket);
    await expect(socket.emitWithAck('room:join', { room: 'bad', role: 'watch', deviceId: 'x' })).resolves.toEqual({ ok: false });
    await expect(socket.emitWithAck('sync:message', envelope())).resolves.toEqual({ delivered: false });

    const watch = await client('ABC123', 'watch');
    await client('ABC123', 'phone');
    await expect(watch.emitWithAck('sync:message', { ...envelope(), payload: { id: 1 } })).resolves.toEqual({ delivered: false });
  });

  it('announces peer counts on join and leave', async () => {
    const watch = await client('ABC123', 'watch');
    const counts: number[] = [];
    watch.on('room:peers', ({ count }: { count: number }) => counts.push(count));
    const phone = await client('ABC123', 'phone');
    await new Promise((resolve) => setTimeout(resolve, 50));
    phone.close();
    await new Promise((resolve) => setTimeout(resolve, 100));
    expect(counts).toEqual([2, 1]);
  });
});
