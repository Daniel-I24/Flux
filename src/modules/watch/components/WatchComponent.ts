import type { Unsubscribe } from '../../../core/types/domain';
import type { Component } from '../../../shared/components/Component';
import type { ConnectionView } from '../../../shared/components/ConnectionBadge';
import { t } from '../../../shared/i18n';
import type { WatchSnapshot, WatchStateId } from '../models/watchTypes';
import type { WatchStateMachine } from '../services/WatchStateMachine';
import { IdleView } from './views/IdleView';
import { ListeningView } from './views/ListeningView';
import { PreviewView } from './views/PreviewView';
import { ProcessingView } from './views/ProcessingView';
import { SavedView } from './views/SavedView';
import type { WatchView, WatchViewContext } from './views/WatchView';
import { WatchFace } from './WatchFace';

type ViewWithOverlay = WatchView & { overlay?: Element };

const VIEW_FACTORIES: Record<WatchStateId, (context: WatchViewContext, snapshot: WatchSnapshot) => ViewWithOverlay> = {
  idle: (context) => new IdleView(context),
  listening: (context, snapshot) => new ListeningView(context, snapshot.simulated),
  processing: () => new ProcessingView(),
  preview: (context) => new PreviewView(context),
  saved: () => new SavedView(),
};

export interface WatchComponentOptions {
  machine: WatchStateMachine;
  maxLength: number;
  autoSaveSeconds: number;
  languageTag: string;
}

/** Binds the state machine to the round screen, swapping one view per state. */
export class WatchComponent implements Component {
  readonly element: HTMLElement;
  private readonly face: WatchFace;
  private view: ViewWithOverlay | null = null;
  private viewState: WatchStateId | null = null;
  private readonly unsubscribe: Unsubscribe;
  private readonly context: WatchViewContext;

  constructor(private readonly options: WatchComponentOptions) {
    this.face = new WatchFace(options.languageTag, t('watch.deviceLabel'));
    this.element = this.face.element;
    this.context = {
      dispatch: (command) => options.machine.dispatch(command),
      maxLength: options.maxLength,
      autoSaveSeconds: options.autoSaveSeconds,
      languageTag: options.languageTag,
    };
    this.element.addEventListener('keydown', this.onKeyDown);
    this.unsubscribe = options.machine.subscribe((snapshot) => this.render(snapshot));
  }

  setConnection(view: ConnectionView): void {
    this.face.setConnection(view);
  }

  destroy(): void {
    this.unsubscribe();
    this.element.removeEventListener('keydown', this.onKeyDown);
    this.view?.destroy();
    this.face.destroy();
  }

  private readonly onKeyDown = (event: KeyboardEvent) => {
    if (event.key === 'Escape') this.options.machine.dispatch({ type: 'cancel' });
  };

  private render(snapshot: WatchSnapshot): void {
    if (snapshot.state !== this.viewState) {
      const hadFocus = this.element.contains(document.activeElement);
      this.view?.destroy();
      this.view = VIEW_FACTORIES[snapshot.state](this.context, snapshot);
      this.viewState = snapshot.state;
      this.face.body.replaceChildren(this.view.element);
      this.face.overlay.replaceChildren(...(this.view.overlay ? [this.view.overlay] : []));
      this.view.update(snapshot);
      if (hadFocus) this.focusPrimaryControl();
      return;
    }
    this.view?.update(snapshot);
  }

  /** Keeps keyboard users inside the watch when the screen changes. */
  private focusPrimaryControl(): void {
    queueMicrotask(() => this.face.body.querySelector<HTMLElement>('button, input')?.focus());
  }
}
