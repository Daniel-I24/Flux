import type { WatchCommand } from '../../models/watchTypes';
import { IdleState } from './IdleState';
import { WatchState } from './WatchState';

/** Confirmation screen (check + haptic feedback), then back to idle. */
export class SavedState extends WatchState {
  readonly id = 'saved' as const;

  override enter(): void {
    const { deps } = this.machine;
    deps.vibrate(deps.timing.vibrationPattern);
    this.disposables.timeout(() => this.machine.transition(new IdleState(this.machine)), deps.timing.savedScreenMs);
  }

  handle(command: WatchCommand): void {
    if (command.type === 'primary' || command.type === 'cancel') {
      this.machine.transition(new IdleState(this.machine));
    }
  }
}
