import { BellRing, Check, Clock } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { h, replaceContent } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';
import { formatClock, parseIsoDate } from '../../../shared/utils/dateUtils';
import type { Note } from '../../notes/models/note';

export interface AlarmBannerHandlers {
  onSnooze(note: Note): void;
  onComplete(note: Note): void;
}

/** Prominent, assertive banner for the ringing reminder with Snooze and Complete. */
export class AlarmBanner {
  readonly element = h('div', { attrs: { role: 'alert', 'aria-live': 'assertive' } });

  constructor(
    private readonly languageTag: string,
    private readonly handlers: AlarmBannerHandlers
  ) {}

  render(note: Note | null, pending: number): void {
    if (!note) return replaceContent(this.element, []);
    const due = parseIsoDate(note.dueAt);
    replaceContent(this.element, [
      h('div', { class: 'animate-slide-down rounded-2xl border border-reminder/50 bg-reminder/15 p-4 shadow-raised' }, [
        h('div', { class: 'flex items-start gap-3' }, [
          h('span', { class: 'grid h-10 w-10 shrink-0 place-items-center rounded-full bg-reminder text-on-primary motion-safe:animate-pulse' }, [icon(BellRing, 20)]),
          h('div', { class: 'min-w-0 flex-1' }, [
            h('p', { class: 'text-xs font-semibold uppercase tracking-wide text-reminder' }, [
              t('phone.alarmTitle'),
              due ? ` · ${formatClock(due, this.languageTag)}` : '',
              pending > 1 ? ` · +${pending - 1}` : '',
            ]),
            h('p', { class: 'mt-0.5 break-words text-base font-semibold text-fg' }, [note.title]),
          ]),
        ]),
        h('div', { class: 'mt-3 grid grid-cols-2 gap-2' }, [
          createButton({ label: t('actions.snooze'), icon: Clock, className: 'px-2', onClick: () => this.handlers.onSnooze(note) }),
          createButton({ label: t('actions.complete'), icon: Check, variant: 'primary', className: 'px-2', onClick: () => this.handlers.onComplete(note) }),
        ]),
      ]),
    ]);
  }
}
