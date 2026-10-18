import { AppEvents, EventKey, EventHandler } from './eventTypes';

/**
 * Strongly typed EventBus implementing the Observer Pattern.
 * Facilitates decoupled communication between the Watch and Phone modules.
 */
export class EventBus {
  private readonly listeners: Map<EventKey, Set<EventHandler<EventKey>>> = new Map();

  public on<K extends EventKey>(event: K, handler: EventHandler<K>): () => void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set());
    }
    const handlers = this.listeners.get(event)!;
    handlers.add(handler as EventHandler<EventKey>);

    return () => {
      handlers.delete(handler as EventHandler<EventKey>);
      if (handlers.size === 0) {
        this.listeners.delete(event);
      }
    };
  }

  public emit<K extends EventKey>(event: K, data: AppEvents[K]): void {
    const handlers = this.listeners.get(event);
    if (handlers) {
      handlers.forEach((handler) => {
        try {
          handler(data);
        } catch (error) {
          console.error(`Error in event handler for ${String(event)}:`, error);
        }
      });
    }
  }

  public clearAll(): void {
    this.listeners.clear();
  }

  public listenerCount(event: EventKey): number {
    return this.listeners.get(event)?.size ?? 0;
  }
}

export const eventBus = new EventBus();
