import { ConnectionBadge, type ConnectionView } from '../../../shared/components/ConnectionBadge';
import { h } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';

/** Fixed header: title, note counter, extensible action buttons and link status. */
export class PhoneHeader {
  readonly element: HTMLElement;
  private readonly count = h('p', { class: 'text-xs text-muted tabular-nums' });
  private readonly badge = new ConnectionBadge();
  private readonly actions = h('div', { class: 'flex items-center gap-1' });

  constructor() {
    this.element = h('header', { class: 'sticky top-0 z-20 border-b border-border/60 bg-bg/90 px-4 pb-3 pt-3 backdrop-blur' }, [
      h('div', { class: 'flex items-center justify-between gap-2' }, [
        h('div', { class: 'min-w-0' }, [
          h('h1', { class: 'truncate text-xl font-bold tracking-tight text-fg' }, [t('phone.title')]),
          this.count,
        ]),
        this.actions,
      ]),
      h('div', { class: 'mt-2' }, [this.badge.element]),
    ]);
  }

  addAction(button: HTMLElement): void {
    this.actions.appendChild(button);
  }

  setCount(total: number): void {
    this.count.textContent = total === 1 ? t('phone.countOne') : t('phone.count', { count: total });
  }

  setConnection(view: ConnectionView): void {
    this.badge.update(view);
  }
}
