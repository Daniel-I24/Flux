import type { NoteCategory } from '../../../core/types/domain';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';
import { CATEGORY_STYLES } from './categoryStyles';

/** Colored pill with icon + category name (color is never the only cue). */
export const createCategoryBadge = (category: NoteCategory, size: 'sm' | 'md' = 'sm'): HTMLElement => {
  const style = CATEGORY_STYLES[category];
  const sizing = size === 'sm' ? 'px-2 py-0.5 text-xs gap-1' : 'px-3 py-1 text-sm gap-1.5';
  return h('span', { class: `inline-flex items-center rounded-full font-semibold ${sizing} ${style.soft} ${style.text}` }, [
    icon(style.icon, size === 'sm' ? 13 : 15),
    h('span', {}, [t(`categories.${category}`)]),
  ]);
};
