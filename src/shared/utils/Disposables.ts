import type { Unsubscribe } from '../../core/types/domain';

/**
 * Collects cleanup callbacks (listeners, timers, subscriptions, streams) so every
 * component and state can release its resources with a single `dispose()` call.
 */
export class Disposables {
  private readonly cleanups: Unsubscribe[] = [];

  add(cleanup: Unsubscribe): void {
    this.cleanups.push(cleanup);
  }

  listen<K extends keyof WindowEventMap>(
    target: Window,
    type: K,
    handler: (event: WindowEventMap[K]) => void,
    options?: AddEventListenerOptions
  ): void;
  listen<K extends keyof DocumentEventMap>(
    target: Document,
    type: K,
    handler: (event: DocumentEventMap[K]) => void,
    options?: AddEventListenerOptions
  ): void;
  listen<K extends keyof HTMLElementEventMap>(
    target: HTMLElement,
    type: K,
    handler: (event: HTMLElementEventMap[K]) => void,
    options?: AddEventListenerOptions
  ): void;
  listen(
    target: EventTarget,
    type: string,
    handler: (event: Event) => void,
    options?: AddEventListenerOptions
  ): void {
    target.addEventListener(type, handler, options);
    this.add(() => target.removeEventListener(type, handler, options));
  }

  interval(callback: () => void, ms: number): void {
    const id = setInterval(callback, ms);
    this.add(() => clearInterval(id));
  }

  timeout(callback: () => void, ms: number): void {
    const id = setTimeout(callback, ms);
    this.add(() => clearTimeout(id));
  }

  dispose(): void {
    while (this.cleanups.length > 0) this.cleanups.pop()?.();
  }
}
