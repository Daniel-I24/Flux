import type { Server, Socket } from 'socket.io';
import type { ServerConfig } from '../config/env.js';
import { logger } from '../config/logger.js';
import { JoinRoomSchema, SyncEnvelopeSchema } from '../schemas/sync.js';
import { RateLimiter } from './RateLimiter.js';

const MINUTE_MS = 60_000;
const ROOM_PREFIX = 'pair:';

type Ack<T> = (response: T) => void;
const noopAck = () => undefined;
const asAck = <T>(value: unknown): Ack<T> => (typeof value === 'function' ? (value as Ack<T>) : noopAck);

interface SocketData {
  room?: string;
}

const peersIn = (io: Server, room: string): number => io.sockets.adapter.rooms.get(room)?.size ?? 0;

const announcePeers = (io: Server, room: string) => io.to(room).emit('room:peers', { count: peersIn(io, room) });

/**
 * Relay for pairing rooms. A socket can only talk inside the room it joined
 * (rooms are isolated by the 6-char code), every message is validated with zod,
 * and the ack tells the sender whether at least one peer received it.
 */
export const registerSyncHandlers = (io: Server, config: ServerConfig): void => {
  io.on('connection', (socket: Socket) => {
    const data = socket.data as SocketData;
    const limiter = new RateLimiter(config.socket.messagesPerMinute, MINUTE_MS);

    socket.on('room:join', async (payload: unknown, ack: unknown) => {
      const reply = asAck<{ ok: boolean; peers?: number }>(ack);
      const parsed = JoinRoomSchema.safeParse(payload);
      if (!parsed.success || !limiter.tryConsume()) return reply({ ok: false });
      const room = `${ROOM_PREFIX}${parsed.data.room}`;
      if (data.room && data.room !== room) {
        await socket.leave(data.room);
        announcePeers(io, data.room);
      }
      data.room = room;
      await socket.join(room);
      announcePeers(io, room);
      return reply({ ok: true, peers: peersIn(io, room) - 1 });
    });

    socket.on('sync:message', (payload: unknown, ack: unknown) => {
      const reply = asAck<{ delivered: boolean }>(ack);
      if (!data.room || !limiter.tryConsume()) return reply({ delivered: false });
      const envelope = SyncEnvelopeSchema.safeParse(payload);
      if (!envelope.success) {
        logger.warn('Rejected invalid sync envelope');
        return reply({ delivered: false });
      }
      const others = peersIn(io, data.room) - 1;
      if (others > 0) socket.to(data.room).emit('sync:message', envelope.data);
      return reply({ delivered: others > 0 });
    });

    socket.on('disconnect', () => {
      if (data.room) announcePeers(io, data.room);
    });
  });
};
