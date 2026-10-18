import { toAppError } from '../../../../core/errors/AppError';
import { SECOND_MS } from '../../../../shared/utils/dateUtils';
import { Disposables } from '../../../../shared/utils/Disposables';
import type { ClassificationResult } from '../../../ai/models/classification';
import type { WatchCommand } from '../../models/watchTypes';
import { IdleState } from './IdleState';
import { ProcessingState } from './ProcessingState';
import { SavedState } from './SavedState';
import { WatchState } from './WatchState';

/** Shows the classified note with a cancellable auto-save countdown. */
export class PreviewState extends WatchState {
  readonly id = 'preview' as const;
  private countdown = new Disposables();
  private saving = false;

  constructor(
    machine: ConstructorParameters<typeof WatchState>[0],
    private readonly result: ClassificationResult
  ) {
    super(machine);
    this.disposables.add(() => this.countdown.dispose());
  }

  override enter(): void {
    this.startCountdown();
  }

  handle(command: WatchCommand): void {
    switch (command.type) {
      case 'confirm':
        return void this.save();
      case 'cancel':
        return this.machine.transition(new IdleState(this.machine));
      case 'edit':
        this.countdown.dispose();
        return this.machine.update({ editing: true });
      case 'cancelEdit':
        this.machine.update({ editing: false });
        return this.startCountdown();
      case 'resubmit':
        return this.machine.transition(new ProcessingState(this.machine, command.text), { transcript: command.text });
      default:
        return undefined;
    }
  }

  private startCountdown(): void {
    this.countdown.dispose();
    this.countdown = new Disposables();
    let secondsLeft = this.machine.deps.timing.autoSaveSeconds;
    this.machine.update({ secondsLeft, editing: false });
    this.countdown.interval(() => {
      secondsLeft -= 1;
      this.machine.update({ secondsLeft });
      if (secondsLeft <= 0) void this.save();
    }, SECOND_MS);
  }

  private async save(): Promise<void> {
    if (this.saving) return;
    this.saving = true;
    this.countdown.dispose();
    try {
      const outcome = await this.machine.deps.save(this.result);
      if (this.machine.isCurrent(this)) {
        this.machine.transition(new SavedState(this.machine), { delivered: outcome.delivered });
      }
    } catch (error) {
      if (this.machine.isCurrent(this)) {
        this.machine.transition(new IdleState(this.machine), { error: toAppError(error).code });
      }
    }
  }
}
