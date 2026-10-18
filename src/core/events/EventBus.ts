import type { Unsubscribe } from '../types/domain';
import { logger } from '../logger/logger';

export type EventHandler<T> = (payload: T) => void;
type ErrorReporter = (event: string, error: unknown) => void;

const defaultReporter: ErrorReporter = (event, error) =>
  logger.error(`Event handler failed for "${event}"`, error);

/**
 * Typed Observer implementation. Each device gets its own instance so two
 * simulated devices on the same page never share internal events.
 * A failing handler never prevents the remaining handlers from running.
 */
export class EventBus<TEvents extends object> {
  private readonly handlers = new Map<keyof TEvents, Set<EventHandler<never>>>();

  constructor(private readonly reportError: ErrorReporter = defaultReporter) {}

  on<K extends keyof TEvents>(event: K, handler: EventHandler<TEvents[K]>): Unsubscribe {
    const set = this.handlers.get(event) ?? new Set<EventHandler<never>>();
    set.add(handler as EventHandler<never>);
    this.handlers.set(event, set);
    return () => this.off(event, handler);
  }

  once<K extends keyof TEvents>(event: K, handler: EventHandler<TEvents[K]>): Unsubscribe {
    const unsubscribe = this.on(event, (payload) => {
      unsubscribe();
      handler(payload);
    });
    return unsubscribe;
  }

  off<K extends keyof TEvents>(event: K, handler: EventHandler<TEvents[K]>): void {
    const set = this.handlers.get(event);
    if (!set) return;
    set.delete(handler as EventHandler<never>);
    if (set.size === 0) this.handlers.delete(event);
  }

  emit<K extends keyof TEvents>(event: K, payload: TEvents[K]): void {
    const set = this.handlers.get(event);
    if (!set) return;
    [...set].forEach((handler) => {
      try {
        (handler as EventHandler<TEvents[K]>)(payload);
      } catch (error) {
        this.reportError(String(event), error);
      }
    });
  }

  listenerCount(event: keyof TEvents): number {
    return this.handlers.get(event)?.size ?? 0;
  }

  clear(): void {
    this.handlers.clear();
  }
}
