import { io, type ManagerOptions, type Socket, type SocketOptions } from 'socket.io-client';
import { z } from 'zod';
import type { DeviceRole } from '../../../../core/types/domain';
import type { SyncEnvelope } from '../../models/syncEnvelope';
import { TransportBase } from './TransportBase';

type SocketFactory = (url: string, options: Partial<ManagerOptions & SocketOptions>) => Socket;

const JoinAckSchema = z.object({ ok: z.boolean(), peers: z.number().int().min(0).optional() });
const DeliveryAckSchema = z.object({ delivered: z.boolean() });
const PeersSchema = z.object({ count: z.number().int().min(0) });

const defaultFactory: SocketFactory = (url, options) => (url ? io(url, options) : io(options));

export interface SocketTransportOptions {
  url: string;
  room: string;
  role: DeviceRole;
  deviceId: string;
  ackTimeoutMs: number;
  factory?: SocketFactory;
}

/** Cross-device transport: Socket.IO room named after the pairing code, with delivery acks. */
export class SocketSyncTransport extends TransportBase {
  readonly name = 'socket';
  private socket: Socket | null = null;
  private everConnected = false;
  private readonly onBrowserOffline = () => this.setState({ status: 'offline', peers: 0 });

  constructor(private readonly options: SocketTransportOptions) {
    super();
  }

  connect(): void {
    if (this.socket) return;
    const socket = (this.options.factory ?? defaultFactory)(this.options.url, {
      path: '/socket.io',
      transports: ['websocket', 'polling'],
      reconnectionDelayMax: 5000,
    });
    this.socket = socket;
    socket.on('connect', () => void this.join());
    socket.on('disconnect', () => this.setState({ status: socket.active ? 'reconnecting' : 'offline', peers: 0 }));
    socket.on('connect_error', () => this.setState({ status: this.everConnected ? 'reconnecting' : 'offline', peers: 0 }));
    socket.on('room:peers', (data: unknown) => {
      const parsed = PeersSchema.safeParse(data);
      if (parsed.success) this.setState({ peers: Math.max(0, parsed.data.count - 1) });
    });
    socket.on('sync:message', (data: unknown) => this.emitMessage(data));
    globalThis.addEventListener?.('offline', this.onBrowserOffline);
  }

  disconnect(): void {
    globalThis.removeEventListener?.('offline', this.onBrowserOffline);
    this.socket?.removeAllListeners();
    this.socket?.disconnect();
    this.socket = null;
    this.setState({ status: 'offline', peers: 0 });
  }

  async send(envelope: SyncEnvelope): Promise<boolean> {
    if (!this.socket || this.state.status !== 'connected') return false;
    try {
      const ack = await this.socket.timeout(this.options.ackTimeoutMs).emitWithAck('sync:message', envelope);
      const parsed = DeliveryAckSchema.safeParse(ack);
      return parsed.success && parsed.data.delivered;
    } catch {
      return false;
    }
  }

  private async join(): Promise<void> {
    const { room, role, deviceId, ackTimeoutMs } = this.options;
    try {
      const ack = await this.socket?.timeout(ackTimeoutMs).emitWithAck('room:join', { room, role, deviceId });
      const parsed = JoinAckSchema.safeParse(ack);
      if (!parsed.success || !parsed.data.ok) return this.setState({ status: 'offline', peers: 0 });
      this.everConnected = true;
      this.setState({ status: 'connected', peers: parsed.data.peers ?? 0 });
    } catch {
      this.setState({ status: 'reconnecting', peers: 0 });
    }
  }
}
