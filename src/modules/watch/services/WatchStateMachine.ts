import type { Unsubscribe } from '../../../core/types/domain';
import {
  INITIAL_SNAPSHOT,
  type WatchCommand,
  type WatchDependencies,
  type WatchSnapshot,
} from '../models/watchTypes';
import { IdleState } from './states/IdleState';
import type { WatchMachineContext, WatchState } from './states/WatchState';

type Listener = (snapshot: WatchSnapshot) => void;

/** Fields reset on every transition so stale data never leaks between states. */
const TRANSIENT_RESET: Partial<WatchSnapshot> = {
  interim: '',
  editing: false,
  textEntry: false,
  error: null,
};

/**
 * Context of the State pattern for the watch: Idle → Listening → Processing →
 * Preview → Saved. Views only dispatch commands and render snapshots.
 */
export class WatchStateMachine implements WatchMachineContext {
  private current: WatchState;
  private state: WatchSnapshot = INITIAL_SNAPSHOT;
  private readonly listeners = new Set<Listener>();

  constructor(readonly deps: WatchDependencies) {
    this.current = new IdleState(this);
    this.current.enter();
  }

  get snapshot(): WatchSnapshot {
    return this.state;
  }

  dispatch(command: WatchCommand): void {
    this.current.handle(command);
  }

  /** Demo shortcut: always starts from idle so presets can interrupt any state. */
  simulate(text: string): void {
    if (this.current.id !== 'idle') this.transition(new IdleState(this));
    this.dispatch({ type: 'simulate', text });
  }

  transition(next: WatchState, patch: Partial<WatchSnapshot> = {}): void {
    this.current.exit();
    this.current = next;
    const reset = next.id === 'idle' ? { result: null, transcript: '', simulated: false, delivered: null } : {};
    this.update({ ...TRANSIENT_RESET, ...reset, ...patch, state: next.id });
    next.enter();
  }

  update(patch: Partial<WatchSnapshot>): void {
    this.state = { ...this.state, ...patch };
    this.listeners.forEach((listener) => listener(this.state));
  }

  isCurrent(state: WatchState): boolean {
    return this.current === state;
  }

  subscribe(listener: Listener): Unsubscribe {
    this.listeners.add(listener);
    listener(this.state);
    return () => this.listeners.delete(listener);
  }

  destroy(): void {
    this.current.exit();
    this.listeners.clear();
  }
}
