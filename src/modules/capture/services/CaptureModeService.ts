import type { EventBus } from '../../../core/events/EventBus';
import type { AppEvents } from '../../../core/events/appEvents';
import type { CaptureMode } from '../../../core/types/domain';
import type { KeyValueStore } from '../../../shared/storage/KeyValueStore';

/** Remembers whether capture uses the real microphone or text input (demo "mic" switch). */
export class CaptureModeService {
  private mode: CaptureMode;

  constructor(
    private readonly store: KeyValueStore,
    private readonly bus: EventBus<AppEvents>,
    private readonly storageKey: string
  ) {
    this.mode = store.get(storageKey) === 'text' ? 'text' : 'voice';
  }

  get current(): CaptureMode {
    return this.mode;
  }

  setMode(mode: CaptureMode): void {
    if (mode === this.mode) return;
    this.mode = mode;
    this.store.set(this.storageKey, mode);
    this.bus.emit('capture:mode-changed', { mode });
  }

  toggle(): void {
    this.setMode(this.mode === 'voice' ? 'text' : 'voice');
  }
}
