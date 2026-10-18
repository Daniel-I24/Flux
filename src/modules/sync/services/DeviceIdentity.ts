import type { DeviceRole } from '../../../core/types/domain';
import type { KeyValueStore } from '../../../shared/storage/KeyValueStore';
import { createId } from '../../../shared/utils/id';

/** Stable per-browser id for each simulated device role (used as message origin). */
export const getDeviceId = (store: KeyValueStore, baseKey: string, role: DeviceRole): string => {
  const key = `${baseKey}.${role}`;
  const existing = store.get(key);
  if (existing) return existing;
  const id = `${role}-${createId()}`;
  store.set(key, id);
  return id;
};
