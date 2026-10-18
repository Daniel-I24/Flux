import type { IconNode } from 'lucide';
import { h } from '../dom/h';
import { icon } from '../dom/icon';

export interface EmptyStateOptions {
  icon: IconNode;
  title: string;
  description?: string;
  tone?: 'neutral' | 'danger';
  action?: HTMLElement;
}

export const createEmptyState = (options: EmptyStateOptions): HTMLElement => {
  const tone = options.tone === 'danger' ? 'bg-danger/10 text-danger' : 'bg-primary/10 text-primary';
  return h('div', { class: 'flex flex-col items-center gap-3 px-6 py-12 text-center animate-fade-in' }, [
    h('div', { class: `grid h-16 w-16 place-items-center rounded-2xl ${tone}` }, [icon(options.icon, 30)]),
    h('p', { class: 'text-base font-semibold text-fg' }, [options.title]),
    options.description ? h('p', { class: 'max-w-xs text-sm text-muted' }, [options.description]) : null,
    options.action ?? null,
  ]);
};

export const createSpinner = (label: string, size = 28): HTMLElement =>
  h('div', { class: 'flex flex-col items-center gap-3 py-10 text-muted', attrs: { role: 'status' } }, [
    h('span', {
      class: 'animate-spin rounded-full border-[3px] border-border border-t-primary',
      style: { width: `${size}px`, height: `${size}px` },
      attrs: { 'aria-hidden': 'true' },
    }),
    h('span', { class: 'text-sm' }, [label]),
  ]);
