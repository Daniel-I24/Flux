import { APP_CONFIG } from '../../core/config/appConfig';
import type { DeviceRole } from '../../core/types/domain';
import { PairingService } from '../../modules/sync/services/PairingService';
import { CompositeSyncTransport } from '../../modules/sync/services/transports/CompositeSyncTransport';
import type { ISyncTransport } from '../../modules/sync/services/transports/ISyncTransport';
import { LocalSyncTransport } from '../../modules/sync/services/transports/LocalSyncTransport';
import { SocketSyncTransport } from '../../modules/sync/services/transports/SocketSyncTransport';
import { browserStorage } from '../../shared/storage/KeyValueStore';
import { channelNameFor, deviceIdFor } from './createDeviceSync';

export const pairingFor = (role: DeviceRole): PairingService =>
  new PairingService(browserStorage, `${APP_CONFIG.storageKeys.pairCode}.${role}`, APP_CONFIG.sync.pairCodeLength);

/** Paired devices: Socket.IO across machines (primary) + BroadcastChannel across tabs. */
export const createPairedTransport = (role: DeviceRole, room: string): ISyncTransport =>
  new CompositeSyncTransport([
    new SocketSyncTransport({
      url: APP_CONFIG.socketUrl,
      room,
      role,
      deviceId: deviceIdFor(role),
      ackTimeoutMs: APP_CONFIG.timeouts.socketAckMs,
    }),
    new LocalSyncTransport(channelNameFor(room), deviceIdFor(role)),
  ]);

export const publicOrigin = (): string => APP_CONFIG.publicOrigin || window.location.origin;
