import { CircleAlert, Inbox, SearchX } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { createEmptyState, createSpinner } from '../../../shared/components/Feedback';
import { h, replaceContent } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';
import type { Note } from '../models/note';
import { createNoteCard, type NoteCardHandlers } from './NoteCard';

export type NoteListState =
  | { kind: 'loading' }
  | { kind: 'error'; retry: () => void }
  | { kind: 'ready'; notes: Note[]; filtered: boolean };

/** Renders the list plus its loading, error, empty and no-results states. */
export class NoteList {
  readonly element = h('div', { class: 'flex flex-col', attrs: { 'aria-busy': 'false' } });
  private readonly renderedIds = new Set<string>();

  constructor(
    private readonly languageTag: string,
    private readonly handlers: NoteCardHandlers
  ) {}

  render(state: NoteListState, now: Date = new Date()): void {
    this.element.setAttribute('aria-busy', String(state.kind === 'loading'));
    if (state.kind === 'loading') return replaceContent(this.element, [createSpinner(t('phone.loading'))]);
    if (state.kind === 'error') {
      return replaceContent(this.element, [
        createEmptyState({
          icon: CircleAlert,
          tone: 'danger',
          title: t('phone.errorTitle'),
          action: createButton({ label: t('actions.retry'), variant: 'primary', onClick: state.retry }),
        }),
      ]);
    }
    if (state.notes.length === 0) {
      return replaceContent(this.element, [
        state.filtered
          ? createEmptyState({ icon: SearchX, title: t('phone.noResultsTitle'), description: t('phone.noResultsDescription') })
          : createEmptyState({ icon: Inbox, title: t('phone.emptyTitle'), description: t('phone.emptyDescription') }),
      ]);
    }
    const focus = this.captureFocus();
    replaceContent(this.element, [
      h(
        'ul',
        { class: 'flex flex-col gap-3', attrs: { 'aria-label': t('phone.listLabel') } },
        state.notes.map((note) =>
          createNoteCard(note, now, this.languageTag, this.handlers, !this.renderedIds.has(note.id))
        )
      ),
    ]);
    state.notes.forEach((note) => this.renderedIds.add(note.id));
    this.restoreFocus(focus);
  }

  /** Remembers which control of which note had focus so re-renders keep keyboard position. */
  private captureFocus(): { noteId: string; index: number } | null {
    const active = document.activeElement;
    const item = active instanceof HTMLElement ? active.closest<HTMLElement>('[data-note-id]') : null;
    if (!item || !this.element.contains(item)) return null;
    const index = [...item.querySelectorAll('button')].findIndex((button) => button === active);
    return { noteId: item.dataset.noteId ?? '', index };
  }

  private restoreFocus(focus: { noteId: string; index: number } | null): void {
    if (!focus) return;
    const item = [...this.element.querySelectorAll<HTMLElement>('[data-note-id]')].find((li) => li.dataset.noteId === focus.noteId);
    const target = item?.querySelectorAll<HTMLElement>('button')[focus.index] ?? this.element.querySelector<HTMLElement>('button');
    target?.focus();
  }
}
