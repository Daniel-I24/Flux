import { ConnectionBadge, type ConnectionView } from '../../../shared/components/ConnectionBadge';
import { h } from '../../../shared/dom/h';
import { formatClock } from '../../../shared/utils/dateUtils';
import { Disposables } from '../../../shared/utils/Disposables';

export const WATCH_SCREEN_SIZE = 360;
const CLOCK_REFRESH_MS = 10_000;

/**
 * Round screen with a fixed safe area: header (time + link status) on top and a
 * padded body that keeps every control inside the inscribed square of the circle.
 */
export class WatchFace {
  readonly element: HTMLElement;
  readonly body: HTMLElement;
  readonly overlay: HTMLElement;
  private readonly clock = h('p', { class: 'text-[15px] font-semibold tabular-nums tracking-wide text-fg' });
  private readonly badge = new ConnectionBadge(true);
  private readonly disposables = new Disposables();

  constructor(private readonly languageTag: string, label: string) {
    this.body = h('div', { class: 'absolute inset-x-[46px] bottom-[40px] top-[78px] flex items-center justify-center' });
    this.overlay = h('div', { class: 'pointer-events-none absolute inset-0' });
    this.element = h(
      'section',
      {
        class:
          'theme-dark relative overflow-hidden rounded-full bg-[radial-gradient(circle_at_50%_35%,rgb(var(--color-surface-2)),rgb(var(--color-bg))_70%)] ' +
          'text-fg ring-1 ring-white/5',
        style: { width: `${WATCH_SCREEN_SIZE}px`, height: `${WATCH_SCREEN_SIZE}px` },
        attrs: { 'aria-label': label },
      },
      [
        this.overlay,
        h('header', { class: 'absolute inset-x-0 top-[24px] flex flex-col items-center gap-1' }, [this.clock, this.badge.element]),
        this.body,
      ]
    );
    this.tick();
    this.disposables.interval(() => this.tick(), CLOCK_REFRESH_MS);
  }

  setConnection(view: ConnectionView): void {
    this.badge.update(view);
  }

  destroy(): void {
    this.disposables.dispose();
    this.element.remove();
  }

  private tick(): void {
    this.clock.textContent = formatClock(new Date(), this.languageTag);
  }
}
