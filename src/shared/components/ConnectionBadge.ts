import type { ConnectionStatus } from '../../core/types/domain';
import { h, replaceContent } from '../dom/h';
import { t } from '../i18n';

const DOT_CLASSES: Record<ConnectionStatus, string> = {
  connected: 'bg-success',
  reconnecting: 'bg-warning animate-pulse',
  offline: 'bg-danger',
};

export interface ConnectionView {
  status: ConnectionStatus;
  /** Overrides the default label (e.g. "Enlace local" or "Esperando…"). */
  label?: string;
  pending?: number;
}

/** Live status pill: dot + text, announced politely to screen readers. */
export class ConnectionBadge {
  readonly element: HTMLElement;

  constructor(compact = false) {
    this.element = h('div', {
      class:
        'inline-flex max-w-full items-center gap-2 rounded-full border border-border bg-surface/80 ' +
        `${compact ? 'px-2 py-0.5 text-[11px]' : 'px-3 py-1 text-xs'} font-medium text-fg`,
      attrs: { role: 'status', 'aria-live': 'polite' },
    });
  }

  update(view: ConnectionView): void {
    const label = view.label ?? t(`connection.${view.status}`);
    replaceContent(this.element, [
      h('span', { class: `h-2 w-2 shrink-0 rounded-full ${DOT_CLASSES[view.status]}`, attrs: { 'aria-hidden': 'true' } }),
      h('span', { class: 'truncate' }, [label]),
      view.pending
        ? h('span', { class: 'shrink-0 text-muted' }, [`· ${t('connection.pending', { count: view.pending })}`])
        : null,
    ]);
  }
}
