import { Plus } from 'lucide';
import { toAppError } from '../../../core/errors/AppError';
import type { CaptureMode, Unsubscribe } from '../../../core/types/domain';
import type { Component } from '../../../shared/components/Component';
import type { ConnectionView } from '../../../shared/components/ConnectionBadge';
import { ToastHost } from '../../../shared/components/Toast';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';
import { Disposables } from '../../../shared/utils/Disposables';
import type { ClassificationResult } from '../../ai/models/classification';
import type { ISpeechService } from '../../capture/services/SpeechService';
import { CategoryFilter } from '../../notes/components/CategoryFilter';
import { NoteList } from '../../notes/components/NoteList';
import { SearchBar } from '../../notes/components/SearchBar';
import type { Note } from '../../notes/models/note';
import { countByCategory, EMPTY_FILTER, filterNotes, type NoteFilter } from '../../notes/services/noteFilters';
import type { NoteService } from '../../notes/services/NoteService';
import { openComposer } from './ComposerSheet';
import { confirmAction } from './confirmDialog';
import { PhoneHeader } from './PhoneHeader';

const RELATIVE_TIME_REFRESH_MS = 5_000;

export interface PhoneComponentOptions {
  notes: NoteService;
  classify(text: string): Promise<ClassificationResult>;
  speech: ISpeechService;
  captureMode: () => CaptureMode;
  languageTag: string;
  maxLength: number;
}

/** Companion app screen: header, alarm slot, search, filters, list and FAB. */
export class PhoneComponent implements Component {
  readonly element: HTMLElement;
  /** Other modules (reminders) mount their banner here without touching this class. */
  readonly alarmSlot = h('div', { class: 'z-10 shrink-0 px-4 pt-3 [&:not(:has(*))]:hidden' });
  readonly header = new PhoneHeader();
  readonly toasts = new ToastHost();
  private readonly list: NoteList;
  private readonly filterChips: CategoryFilter;
  private readonly search: SearchBar;
  private readonly disposables = new Disposables();
  private filter: NoteFilter = EMPTY_FILTER;
  private notes: Note[] = [];
  private unsubscribe: Unsubscribe | null = null;

  constructor(private readonly options: PhoneComponentOptions) {
    this.list = new NoteList(options.languageTag, {
      onToggle: (note) => void this.run(() => options.notes.setCompleted(note.id, !note.completed), note.completed ? undefined : t('phone.toastCompleted')),
      onDelete: (note) => this.confirmDelete(note),
    });
    this.filterChips = new CategoryFilter((category) => this.setFilter({ category }));
    this.search = new SearchBar((query) => this.setFilter({ query }));

    const fab = h(
      'button',
      {
        class: 'absolute bottom-6 right-5 z-30 grid h-14 w-14 place-items-center rounded-2xl bg-primary text-on-primary shadow-raised transition-transform hover:scale-105 active:scale-95',
        style: { marginBottom: 'env(safe-area-inset-bottom)' },
        attrs: { type: 'button', 'aria-label': t('phone.fab') },
        on: { click: () => this.openComposer() },
      },
      [icon(Plus, 26, '', 2.5)]
    );

    this.element = h('div', { class: 'relative flex h-full min-h-0 flex-col overflow-hidden bg-bg text-fg' }, [
      this.header.element,
      this.alarmSlot,
      h('div', { class: 'flex min-h-0 flex-1 flex-col gap-3 overflow-y-auto overscroll-contain pb-28 pt-3 [&>*]:shrink-0' }, [
        h('div', { class: 'px-4' }, [this.search.element]),
        this.filterChips.element,
        h('div', { class: 'px-4' }, [this.list.element]),
      ]),
      fab,
      this.toasts.element,
    ]);

    this.disposables.interval(() => this.render(), RELATIVE_TIME_REFRESH_MS);
    void this.load();
  }

  setConnection(view: ConnectionView): void {
    this.header.setConnection(view);
  }

  destroy(): void {
    this.unsubscribe?.();
    this.disposables.dispose();
    this.search.destroy();
    this.toasts.destroy();
    this.element.remove();
  }

  private async load(): Promise<void> {
    this.list.render({ kind: 'loading' });
    try {
      this.notes = this.options.notes.isLoaded ? this.options.notes.list() : await this.options.notes.load();
      this.unsubscribe ??= this.options.notes.on('notes:changed', ({ notes }) => {
        this.notes = notes;
        this.render();
      });
      this.render();
    } catch {
      this.list.render({ kind: 'error', retry: () => void this.load() });
    }
  }

  private setFilter(patch: Partial<NoteFilter>): void {
    this.filter = { ...this.filter, ...patch };
    this.render();
  }

  private render(): void {
    const visible = filterNotes(this.notes, this.filter);
    const filtered = this.filter.category !== 'all' || this.filter.query.trim() !== '';
    this.header.setCount(this.notes.length);
    this.filterChips.render(this.filter.category, countByCategory(this.notes, this.filter.query));
    this.list.render({ kind: 'ready', notes: visible, filtered });
  }

  private confirmDelete(note: Note): void {
    confirmAction(this.element, {
      title: t('phone.deleteTitle'),
      description: t('phone.deleteDescription'),
      confirmLabel: t('actions.delete'),
      onConfirm: () => void this.run(() => this.options.notes.remove(note.id), t('phone.toastDeleted')),
    });
  }

  private openComposer(): void {
    const { classify, notes, speech, captureMode, maxLength } = this.options;
    openComposer(this.element, {
      speech,
      captureMode,
      maxLength,
      submit: async (text) => {
        await notes.create(await classify(text), 'phone');
        this.toasts.show(t('phone.toastCreated'));
      },
    });
  }

  private async run(action: () => Promise<unknown>, successMessage?: string): Promise<void> {
    try {
      await action();
      if (successMessage) this.toasts.show(successMessage);
    } catch (error) {
      this.toasts.show(t(`errors.${toAppError(error).code}`));
    }
  }
}
