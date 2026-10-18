import type { WatchCommand, WatchSnapshot } from '../../models/watchTypes';

/** One renderer per state; `update` patches in place so focus is preserved. */
export interface WatchView {
  readonly element: HTMLElement;
  update(snapshot: WatchSnapshot): void;
  destroy(): void;
}

export interface WatchViewContext {
  dispatch(command: WatchCommand): void;
  maxLength: number;
  autoSaveSeconds: number;
  languageTag: string;
}

export const VIEW_BASE_CLASS = 'flex h-full w-full flex-col items-center justify-center gap-3 text-center animate-fade-in';
