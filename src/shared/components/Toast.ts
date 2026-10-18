import { h } from '../dom/h';

const VISIBLE_MS = 2600;

/** Polite, auto-dismissing status messages scoped to a host element. */
export class ToastHost {
  readonly element: HTMLElement;
  private hideTimer: ReturnType<typeof setTimeout> | null = null;

  constructor(positionClass = 'bottom-24') {
    this.element = h('div', {
      class: `pointer-events-none absolute inset-x-0 ${positionClass} z-50 flex justify-center px-4`,
      attrs: { role: 'status', 'aria-live': 'polite' },
    });
  }

  show(message: string): void {
    this.clearTimer();
    this.element.replaceChildren(
      h('div', { class: 'rounded-full bg-fg px-4 py-2 text-sm font-medium text-bg shadow-raised animate-slide-up' }, [
        message,
      ])
    );
    this.hideTimer = setTimeout(() => this.element.replaceChildren(), VISIBLE_MS);
  }

  destroy(): void {
    this.clearTimer();
    this.element.remove();
  }

  private clearTimer(): void {
    if (this.hideTimer) clearTimeout(this.hideTimer);
    this.hideTimer = null;
  }
}
