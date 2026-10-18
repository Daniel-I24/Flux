import { h, type Child } from '../dom/h';
import { createId } from '../utils/id';

const FOCUSABLE =
  'button:not([disabled]), [href], input:not([disabled]), textarea:not([disabled]), select, [tabindex]:not([tabindex="-1"])';

export interface OverlayOptions {
  title: string;
  description?: string;
  /** Builds the body; receives `close` so inner actions can dismiss the overlay. */
  body: (close: () => void) => Child[];
  placement?: 'center' | 'bottom';
  onClose?: () => void;
}

export interface OverlayHandle {
  close(): void;
}

/**
 * Accessible modal scoped to a host element (so it stays inside a device frame):
 * labelled dialog, focus trap, Escape to close and focus restoration.
 */
export const openOverlay = (host: HTMLElement, options: OverlayOptions): OverlayHandle => {
  const titleId = `dlg-${createId()}`;
  const previousFocus = document.activeElement as HTMLElement | null;
  let closed = false;

  const close = () => {
    if (closed) return;
    closed = true;
    backdrop.remove();
    options.onClose?.();
    previousFocus?.focus?.();
  };

  const isBottom = options.placement === 'bottom';
  const panel = h(
    'div',
    {
      class:
        'w-full max-w-sm bg-surface p-5 shadow-raised ' +
        (isBottom ? 'rounded-t-3xl pb-8 animate-slide-up' : 'mx-4 rounded-3xl animate-pop-in'),
      attrs: { role: 'dialog', 'aria-modal': 'true', 'aria-labelledby': titleId },
    },
    [
      h('h2', { class: 'text-lg font-semibold text-fg', attrs: { id: titleId } }, [options.title]),
      options.description ? h('p', { class: 'mt-1 text-sm text-muted' }, [options.description]) : null,
      h('div', { class: 'mt-4' }, options.body(close)),
    ]
  );

  const backdrop = h(
    'div',
    {
      class: `absolute inset-0 z-40 flex bg-black/50 animate-fade-in ${isBottom ? 'items-end justify-center' : 'items-center justify-center'}`,
      on: {
        click: (event) => {
          if (event.target === backdrop) close();
        },
        keydown: (event) => {
          if (event.key === 'Escape') {
            event.stopPropagation();
            close();
          } else if (event.key === 'Tab') {
            trapFocus(panel, event);
          }
        },
      },
    },
    [panel]
  );

  host.appendChild(backdrop);
  (panel.querySelector<HTMLElement>(FOCUSABLE) ?? panel).focus();
  return { close };
};

const trapFocus = (container: HTMLElement, event: KeyboardEvent): void => {
  const items = [...container.querySelectorAll<HTMLElement>(FOCUSABLE)];
  const first = items[0];
  const last = items[items.length - 1];
  if (!first || !last) return;
  if (event.shiftKey && document.activeElement === first) {
    event.preventDefault();
    last.focus();
  } else if (!event.shiftKey && document.activeElement === last) {
    event.preventDefault();
    first.focus();
  }
};
