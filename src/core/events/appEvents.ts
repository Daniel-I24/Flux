import type { AiEngine, CaptureMode, DeviceRole, Locale, ThemeMode } from '../types/domain';
import { EventBus } from './EventBus';

/** Page-wide events (settings and cross-device visual feedback). */
export interface AppEvents {
  'locale:changed': { locale: Locale };
  'theme:changed': { theme: ThemeMode };
  'capture:mode-changed': { mode: CaptureMode };
  'ai:status-changed': { remoteAvailable: boolean; lastEngine: AiEngine | null };
  'sync:activity': { role: DeviceRole; direction: 'out' | 'in' };
}

export const appEvents = new EventBus<AppEvents>();
