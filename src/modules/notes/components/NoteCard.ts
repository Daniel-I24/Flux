import { CalendarClock, Check, Smartphone, Trash2, Watch } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';
import { formatDueDate, formatRelativeTime, parseIsoDate } from '../../../shared/utils/dateUtils';
import type { Note } from '../models/note';
import { createCategoryBadge } from './CategoryBadge';
import { CATEGORY_STYLES } from './categoryStyles';

export interface NoteCardHandlers {
  onToggle(note: Note): void;
  onDelete(note: Note): void;
}

const dueLine = (note: Note, now: Date, languageTag: string): HTMLElement | null => {
  const due = parseIsoDate(note.dueAt);
  if (!due) return null;
  const overdue = !note.completed && due.getTime() <= now.getTime();
  return h('p', { class: `mt-2 flex items-center gap-1.5 text-xs font-semibold ${overdue ? 'text-danger' : 'text-accent'}` }, [
    icon(CalendarClock, 14),
    h('time', { attrs: { datetime: note.dueAt } }, [formatDueDate(due, now, languageTag)]),
  ]);
};

const toggleButton = (note: Note, handlers: NoteCardHandlers): HTMLButtonElement =>
  h(
    'button',
    {
      class:
        'tap-target grid shrink-0 place-items-center rounded-full transition-colors ' +
        (note.completed ? 'text-success' : 'text-muted hover:text-fg'),
      attrs: { type: 'button', 'aria-pressed': String(note.completed), 'aria-label': note.completed ? t('phone.markPending') : t('phone.markComplete') },
      on: { click: () => handlers.onToggle(note) },
    },
    [
      h('span', {
        class: `grid h-6 w-6 place-items-center rounded-full border-2 ${note.completed ? 'border-success bg-success text-on-primary' : 'border-current'}`,
      }, [note.completed ? icon(Check, 14, '', 3) : null]),
    ]
  );

/** One note: category, title, text, relative dates, source device and actions. */
export const createNoteCard = (
  note: Note,
  now: Date,
  languageTag: string,
  handlers: NoteCardHandlers,
  animate = false
): HTMLLIElement => {
  const style = CATEGORY_STYLES[note.category];
  const created = parseIsoDate(note.createdAt) ?? now;
  const showText = note.text.trim().toLocaleLowerCase() !== note.title.trim().toLocaleLowerCase();
  return h('li', { class: animate ? 'animate-slide-up' : '', attrs: { 'data-note-id': note.id } }, [
    h(
      'article',
      {
        class:
          `relative flex items-start gap-2 overflow-hidden rounded-2xl border border-border bg-surface py-3 pl-2 pr-1 shadow-card ` +
          (note.completed ? 'opacity-70' : ''),
        attrs: { 'aria-label': note.title },
      },
      [
        h('span', { class: `absolute inset-y-0 left-0 w-1 ${style.dot}`, attrs: { 'aria-hidden': 'true' } }),
        toggleButton(note, handlers),
        h('div', { class: 'min-w-0 flex-1 py-1' }, [
          h('div', { class: 'flex flex-wrap items-center gap-x-2 gap-y-1' }, [
            createCategoryBadge(note.category),
            h('span', { class: 'inline-flex items-center gap-1 text-xs text-muted' }, [
              icon(note.source === 'watch' ? Watch : Smartphone, 12),
              note.source === 'watch' ? t('phone.fromWatch') : t('phone.fromPhone'),
              ' · ',
              h('time', { attrs: { datetime: note.createdAt } }, [formatRelativeTime(created, now, languageTag)]),
            ]),
          ]),
          h('h3', { class: `mt-1.5 break-words text-[15px] font-semibold leading-snug text-fg ${note.completed ? 'line-through' : ''}` }, [note.title]),
          showText ? h('p', { class: 'mt-0.5 line-clamp-2 break-words text-sm text-muted' }, [note.text]) : null,
          dueLine(note, now, languageTag),
        ]),
        createButton({
          label: t('phone.deleteNote'),
          icon: Trash2,
          iconOnly: true,
          variant: 'ghost',
          className: 'text-muted hover:text-danger',
          onClick: () => handlers.onDelete(note),
        }),
      ]
    ),
  ]);
};
