import { TransportBase } from './TransportBase';

/** Used by an unpaired phone: always offline, every message stays queued. */
export class NullSyncTransport extends TransportBase {
  readonly name = 'none';

  connect(): void {
    this.setState({ status: 'offline', peers: 0 });
  }

  disconnect(): void {}

  async send(): Promise<boolean> {
    return false;
  }
}
