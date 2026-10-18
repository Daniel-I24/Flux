import { NOTE_CATEGORIES } from '../../../core/types/domain';
import { createChip } from '../../../shared/components/Chip';
import { h, replaceContent } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';
import type { CategoryFilter as Filter } from '../services/noteFilters';
import { CATEGORY_STYLES } from './categoryStyles';

const OPTIONS: Filter[] = ['all', ...NOTE_CATEGORIES];

/** Horizontally scrollable category chips with counters. */
export class CategoryFilter {
  readonly element = h('div', {
    class: 'flex gap-2 overflow-x-auto px-4 pb-1 [scrollbar-width:none] [&::-webkit-scrollbar]:hidden',
    attrs: { role: 'group', 'aria-label': t('phone.filtersLabel') },
  });

  constructor(private readonly onSelect: (filter: Filter) => void) {}

  render(selected: Filter, counts: Record<Filter, number>): void {
    const focusedIndex = [...this.element.children].findIndex((child) => child === document.activeElement);
    replaceContent(
      this.element,
      OPTIONS.map((option) =>
        createChip({
          label: t(`categoriesPlural.${option}`),
          count: counts[option],
          pressed: option === selected,
          tone: option === 'all' ? undefined : CATEGORY_STYLES[option],
          showDot: option !== 'all',
          onClick: () => this.onSelect(option),
        })
      )
    );
    if (focusedIndex >= 0) (this.element.children[focusedIndex] as HTMLElement | undefined)?.focus();
  }
}
