import { APP_CONFIG } from '../../core/config/appConfig';
import { appEvents } from '../../core/events/appEvents';
import type { DeviceRole } from '../../core/types/domain';
import { NoteSchema, type Note } from '../../modules/notes/models/note';
import { getDeviceId } from '../../modules/sync/services/DeviceIdentity';
import { Outbox } from '../../modules/sync/services/Outbox';
import { SyncService } from '../../modules/sync/services/SyncService';
import type { ISyncTransport } from '../../modules/sync/services/transports/ISyncTransport';
import { browserStorage } from '../../shared/storage/KeyValueStore';

export const deviceIdFor = (role: DeviceRole): string =>
  getDeviceId(browserStorage, APP_CONFIG.storageKeys.deviceId, role);

export const channelNameFor = (roomCode: string): string => `${APP_CONFIG.sync.channelPrefix}.${roomCode}`;

/** One SyncService per simulated device, with its own persistent outbox per room. */
export const createDeviceSync = (role: DeviceRole, roomCode: string, transport: ISyncTransport): SyncService<Note> =>
  new SyncService<Note>({
    transport,
    outbox: new Outbox(browserStorage, `${APP_CONFIG.storageKeys.outbox}.${role}.${roomCode}`),
    deviceId: deviceIdFor(role),
    payloadSchema: NoteSchema,
    keyOf: (note) => note.id,
    seenLimit: APP_CONFIG.sync.seenMessagesLimit,
    onActivity: (direction) => appEvents.emit('sync:activity', { role, direction }),
  });
