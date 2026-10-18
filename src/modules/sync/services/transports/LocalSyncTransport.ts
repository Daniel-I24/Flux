import { z } from 'zod';
import type { SyncEnvelope } from '../../models/syncEnvelope';
import { TransportBase } from './TransportBase';

export interface ChannelLike {
  postMessage(message: unknown): void;
  close(): void;
  onmessage: ((event: MessageEvent) => void) | null;
}

export type ChannelFactory = (name: string) => ChannelLike;

const FrameSchema = z.discriminatedUnion('type', [
  z.object({ type: z.literal('presence'), from: z.string(), action: z.enum(['hello', 'here', 'bye']) }),
  z.object({ type: z.literal('envelope'), from: z.string(), envelope: z.unknown() }),
]);

type Frame = z.infer<typeof FrameSchema>;

const defaultChannelFactory: ChannelFactory = (name) => new BroadcastChannel(name);

/**
 * Same-browser transport over BroadcastChannel: covers two devices in the same
 * tab (/demo) and in different tabs. A hello/here/bye handshake tracks peers so
 * `send` can report real delivery.
 */
export class LocalSyncTransport extends TransportBase {
  readonly name = 'local';
  private channel: ChannelLike | null = null;
  private readonly peers = new Set<string>();
  private readonly onPageHide = () => this.post({ type: 'presence', from: this.deviceId, action: 'bye' });

  constructor(
    private readonly channelName: string,
    private readonly deviceId: string,
    private readonly createChannel: ChannelFactory = defaultChannelFactory
  ) {
    super();
  }

  connect(): void {
    if (this.channel) return;
    this.channel = this.createChannel(this.channelName);
    this.channel.onmessage = (event) => this.handleFrame(event.data);
    globalThis.addEventListener?.('pagehide', this.onPageHide);
    this.setState({ status: 'connected', peers: 0 });
    this.post({ type: 'presence', from: this.deviceId, action: 'hello' });
  }

  disconnect(): void {
    if (!this.channel) return;
    this.onPageHide();
    globalThis.removeEventListener?.('pagehide', this.onPageHide);
    this.channel.onmessage = null;
    this.channel.close();
    this.channel = null;
    this.peers.clear();
    this.setState({ status: 'offline', peers: 0 });
  }

  async send(envelope: SyncEnvelope): Promise<boolean> {
    if (!this.channel || this.peers.size === 0) return false;
    this.post({ type: 'envelope', from: this.deviceId, envelope });
    return true;
  }

  private post(frame: Frame): void {
    this.channel?.postMessage(frame);
  }

  private handleFrame(data: unknown): void {
    const parsed = FrameSchema.safeParse(data);
    if (!parsed.success || parsed.data.from === this.deviceId) return;
    const frame = parsed.data;
    if (frame.type === 'envelope') {
      this.peers.add(frame.from);
      this.syncPeers();
      this.emitMessage(frame.envelope);
      return;
    }
    if (frame.action === 'bye') this.peers.delete(frame.from);
    else this.peers.add(frame.from);
    if (frame.action === 'hello') this.post({ type: 'presence', from: this.deviceId, action: 'here' });
    this.syncPeers();
  }

  private syncPeers(): void {
    this.setState({ peers: this.peers.size });
  }
}
