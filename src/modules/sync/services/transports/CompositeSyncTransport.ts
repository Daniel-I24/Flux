import type { Unsubscribe } from '../../../../core/types/domain';
import type { SyncEnvelope, TransportState } from '../../models/syncEnvelope';
import type { ISyncTransport } from './ISyncTransport';
import { TransportBase } from './TransportBase';

/**
 * Sends through every child transport (e.g. Socket.IO + BroadcastChannel) so
 * devices sync across machines and still sync between tabs if the server is down.
 * Duplicates are harmless: SyncService de-duplicates by envelope id.
 */
export class CompositeSyncTransport extends TransportBase {
  readonly name: string;
  private subscriptions: Unsubscribe[] = [];

  constructor(private readonly children: ISyncTransport[]) {
    super();
    this.name = children.map((child) => child.name).join('+');
  }

  connect(): void {
    this.subscriptions = this.children.flatMap((child) => [
      child.onMessage((data) => this.emitMessage(data)),
      child.onStateChange(() => this.recompute()),
    ]);
    this.children.forEach((child) => child.connect());
    this.recompute();
  }

  disconnect(): void {
    this.subscriptions.splice(0).forEach((unsubscribe) => unsubscribe());
    this.children.forEach((child) => child.disconnect());
    this.recompute();
  }

  async send(envelope: SyncEnvelope): Promise<boolean> {
    const results = await Promise.all(this.children.map((child) => child.send(envelope).catch(() => false)));
    return results.some(Boolean);
  }

  /**
   * A child that actually reaches a peer wins; otherwise the primary (first) child
   * decides, so a lone local channel never masks a server that is down.
   */
  private recompute(): void {
    const reaching = this.children.filter((child) => child.state.status === 'connected' && child.state.peers > 0);
    if (reaching.length > 0) {
      this.setState({ status: 'connected', peers: Math.max(...reaching.map((child) => child.state.peers)) });
      return;
    }
    const primary: TransportState = this.children[0]?.state ?? { status: 'offline', peers: 0 };
    this.setState({ status: primary.status, peers: 0 });
  }
}
