import type { SyncEnvelope } from '../models/syncEnvelope';
import type { ChannelFactory, ChannelLike } from '../services/transports/LocalSyncTransport';
import { TransportBase } from '../services/transports/TransportBase';

/** In-memory BroadcastChannel replacement delivering synchronously to other members. */
export const createChannelHub = (): ChannelFactory => {
  const members = new Map<string, Set<ChannelLike>>();
  return (name) => {
    const group = members.get(name) ?? new Set<ChannelLike>();
    members.set(name, group);
    const channel: ChannelLike = {
      onmessage: null,
      postMessage(message) {
        group.forEach((member) => {
          if (member !== channel) member.onmessage?.({ data: structuredClone(message) } as MessageEvent);
        });
      },
      close() {
        group.delete(channel);
      },
    };
    group.add(channel);
    return channel;
  };
};

/** Controllable transport for SyncService tests. */
export class FakeTransport extends TransportBase {
  readonly name = 'fake';
  readonly sent: SyncEnvelope[] = [];
  deliver = true;

  connect(): void {
    this.setState({ status: 'connected' });
  }

  disconnect(): void {
    this.setState({ status: 'offline', peers: 0 });
  }

  async send(envelope: SyncEnvelope): Promise<boolean> {
    if (!this.deliver || this.state.status !== 'connected') return false;
    this.sent.push(envelope);
    return true;
  }

  receive(data: unknown): void {
    this.emitMessage(data);
  }

  goOnline(peers = 1): void {
    this.setState({ status: 'connected', peers });
  }

  goOffline(): void {
    this.setState({ status: 'reconnecting', peers: 0 });
  }
}
