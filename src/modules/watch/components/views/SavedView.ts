import { Check, CloudOff } from 'lucide';
import { h, replaceContent } from '../../../../shared/dom/h';
import { icon } from '../../../../shared/dom/icon';
import { t } from '../../../../shared/i18n';
import type { WatchSnapshot } from '../../models/watchTypes';
import { VIEW_BASE_CLASS, type WatchView } from './WatchView';

export class SavedView implements WatchView {
  private readonly status = h('p', { class: 'flex items-center gap-1.5 text-xs font-medium text-muted' });

  readonly element = h('div', { class: VIEW_BASE_CLASS, attrs: { role: 'status', 'aria-live': 'assertive' } }, [
    h('div', { class: 'grid h-24 w-24 place-items-center rounded-full bg-success text-on-primary shadow-raised animate-pop-in' }, [
      icon(Check, 48, '', 3),
    ]),
    h('p', { class: 'text-lg font-semibold text-fg' }, [t('watch.saved')]),
    this.status,
  ]);

  update(snapshot: WatchSnapshot): void {
    const queued = snapshot.delivered === false;
    replaceContent(this.status, [
      queued ? icon(CloudOff, 14, 'text-warning') : null,
      queued ? t('watch.queued') : t('watch.sentToPhone'),
    ]);
  }

  destroy(): void {
    this.element.remove();
  }
}
