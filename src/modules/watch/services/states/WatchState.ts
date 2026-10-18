import { Disposables } from '../../../../shared/utils/Disposables';
import type { WatchCommand, WatchSnapshot, WatchStateId } from '../../models/watchTypes';
import type { WatchDependencies } from '../../models/watchTypes';

/** What a state can ask of the machine (keeps states decoupled from its internals). */
export interface WatchMachineContext {
  readonly deps: WatchDependencies;
  readonly snapshot: WatchSnapshot;
  transition(next: WatchState, patch?: Partial<WatchSnapshot>): void;
  update(patch: Partial<WatchSnapshot>): void;
  isCurrent(state: WatchState): boolean;
}

/**
 * State pattern base: each concrete state reacts only to the commands that make
 * sense for it, and releases its timers/sessions when exited.
 */
export abstract class WatchState {
  abstract readonly id: WatchStateId;
  protected readonly disposables = new Disposables();

  constructor(protected readonly machine: WatchMachineContext) {}

  enter(): void {}

  exit(): void {
    this.disposables.dispose();
  }

  abstract handle(command: WatchCommand): void;
}
