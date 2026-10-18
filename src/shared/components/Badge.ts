import { NoteCategory } from '../../core/types/global';
import { t } from '../i18n';

export interface BadgeOptions {
  category: NoteCategory | string;
  customLabel?: string;
  className?: string;
}

export function createCategoryBadge(options: BadgeOptions): HTMLElement {
  const badge = document.createElement('span');
  const cat = options.category as NoteCategory;
  const label = options.customLabel || t(`categories.${cat}`);

  let colorClasses = 'bg-slate-500/10 text-slate-400 border-slate-500/20';
  if (cat === NoteCategory.TASK) {
    colorClasses = 'bg-category-task-bg text-category-task border-category-task-border';
  } else if (cat === NoteCategory.IDEA) {
    colorClasses = 'bg-category-idea-bg text-category-idea border-category-idea-border';
  } else if (cat === NoteCategory.REMINDER) {
    colorClasses = 'bg-category-reminder-bg text-category-reminder border-category-reminder-border';
  } else if (cat === NoteCategory.INFO) {
    colorClasses = 'bg-category-info-bg text-category-info border-category-info-border';
  }

  badge.className = `inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-xs font-semibold tracking-wide border uppercase ${colorClasses} ${options.className || ''}`;
  badge.textContent = label;
  badge.setAttribute('role', 'status');
  return badge;
}
