import type { Unsubscribe } from '../types/domain';
import { logger } from '../logger/logger';

export interface ViewHandle {
  destroy(): void;
}

export interface RouteContext {
  query: URLSearchParams;
  navigate(path: string): void;
}

export type ViewFactory = (outlet: HTMLElement, context: RouteContext) => ViewHandle;

export interface RouteDefinition {
  path: string;
  /** Lazy loader: each route becomes its own chunk (code splitting). */
  load: () => Promise<ViewFactory>;
}

export interface RouterHooks {
  onLoading?(outlet: HTMLElement): void;
  onError?(outlet: HTMLElement, error: unknown, retry: () => void): void;
  onNavigated?(path: string): void;
}

/**
 * History API router. Only one view is mounted at a time; the previous one is
 * destroyed first so its listeners, timers, sockets and audio streams are released.
 */
export class Router {
  private current: ViewHandle | null = null;
  private renderToken = 0;

  constructor(
    private readonly outlet: HTMLElement,
    private readonly routes: RouteDefinition[],
    private readonly fallbackPath: string,
    private readonly hooks: RouterHooks = {},
    private readonly win: Window = window
  ) {}

  start(): Unsubscribe {
    const onPopState = () => void this.render();
    this.win.addEventListener('popstate', onPopState);
    void this.render();
    return () => {
      this.win.removeEventListener('popstate', onPopState);
      this.unmount();
    };
  }

  navigate(path: string): void {
    const target = new URL(path, this.win.location.origin);
    const next = `${target.pathname}${target.search}`;
    if (next !== `${this.win.location.pathname}${this.win.location.search}`) {
      this.win.history.pushState(null, '', next);
    }
    void this.render();
  }

  /** Re-mounts the active view (used after a language change). */
  refresh(): void {
    void this.render();
  }

  resolve(pathname: string): RouteDefinition | undefined {
    const normalized = pathname.replace(/\/+$/, '') || '/';
    return this.routes.find((route) => route.path === normalized);
  }

  private async render(): Promise<void> {
    const token = ++this.renderToken;
    const route = this.resolve(this.win.location.pathname);
    if (!route) {
      this.win.history.replaceState(null, '', this.fallbackPath);
      return this.render();
    }
    this.unmount();
    this.hooks.onLoading?.(this.outlet);
    try {
      const factory = await route.load();
      if (token !== this.renderToken) return;
      this.outlet.replaceChildren();
      this.current = factory(this.outlet, {
        query: new URLSearchParams(this.win.location.search),
        navigate: (path) => this.navigate(path),
      });
      this.hooks.onNavigated?.(route.path);
    } catch (error) {
      if (token !== this.renderToken) return;
      logger.error(`Route "${route.path}" failed to load`, error);
      this.hooks.onError?.(this.outlet, error, () => void this.render());
    }
  }

  private unmount(): void {
    this.current?.destroy();
    this.current = null;
  }
}
