import { toAppError } from '../../../../core/errors/AppError';
import type { WatchCommand } from '../../models/watchTypes';
import { IdleState } from './IdleState';
import { PreviewState } from './PreviewState';
import { WatchState } from './WatchState';

/** Waits for the AI classification; stale answers (after cancel) are ignored. */
export class ProcessingState extends WatchState {
  readonly id = 'processing' as const;

  constructor(
    machine: ConstructorParameters<typeof WatchState>[0],
    private readonly text: string
  ) {
    super(machine);
  }

  override enter(): void {
    this.machine.deps
      .classify(this.text)
      .then((result) => {
        if (this.machine.isCurrent(this)) this.machine.transition(new PreviewState(this.machine, result), { result });
      })
      .catch((error: unknown) => {
        if (!this.machine.isCurrent(this)) return;
        this.machine.transition(new IdleState(this.machine), { error: toAppError(error).code, textEntry: true });
      });
  }

  handle(command: WatchCommand): void {
    if (command.type === 'cancel') this.machine.transition(new IdleState(this.machine));
  }
}
