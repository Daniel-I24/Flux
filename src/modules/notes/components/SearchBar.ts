import { Search, X } from 'lucide';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';

const DEBOUNCE_MS = 150;
const MAX_QUERY_LENGTH = 80;

/** Debounced search field with a clear button. */
export class SearchBar {
  readonly element: HTMLElement;
  private readonly input: HTMLInputElement;
  private readonly clearButton: HTMLButtonElement;
  private timer: ReturnType<typeof setTimeout> | null = null;

  constructor(private readonly onChange: (query: string) => void) {
    this.input = h('input', {
      class:
        'h-11 w-full rounded-xl border border-border bg-surface pl-10 pr-11 text-[15px] text-fg placeholder:text-muted ' +
        '[&::-webkit-search-cancel-button]:hidden',
      attrs: { type: 'search', placeholder: t('phone.searchPlaceholder'), 'aria-label': t('phone.searchLabel'), maxlength: MAX_QUERY_LENGTH, enterkeyhint: 'search' },
      on: { input: () => this.schedule() },
    });
    this.clearButton = h(
      'button',
      {
        class: 'tap-target absolute right-0 top-1/2 hidden -translate-y-1/2 place-items-center text-muted hover:text-fg',
        attrs: { type: 'button', 'aria-label': t('phone.clearSearch') },
        on: { click: () => this.clear() },
      },
      [icon(X, 18)]
    );
    this.element = h('div', { class: 'relative', attrs: { role: 'search' } }, [
      h('span', { class: 'pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-muted' }, [icon(Search, 18)]),
      this.input,
      this.clearButton,
    ]);
  }

  destroy(): void {
    if (this.timer) clearTimeout(this.timer);
  }

  private schedule(): void {
    this.clearButton.classList.toggle('hidden', this.input.value === '');
    this.clearButton.classList.toggle('grid', this.input.value !== '');
    if (this.timer) clearTimeout(this.timer);
    this.timer = setTimeout(() => this.onChange(this.input.value), DEBOUNCE_MS);
  }

  private clear(): void {
    this.input.value = '';
    this.schedule();
    this.input.focus();
  }
}
