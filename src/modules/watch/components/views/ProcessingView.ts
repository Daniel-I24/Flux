import { Sparkles } from 'lucide';
import { h } from '../../../../shared/dom/h';
import { icon } from '../../../../shared/dom/icon';
import { t } from '../../../../shared/i18n';
import type { WatchSnapshot } from '../../models/watchTypes';
import { VIEW_BASE_CLASS, type WatchView } from './WatchView';

const orbit = (sizeClass: string, animation: string, dotClass: string) =>
  h('div', { class: `absolute rounded-full border border-primary/25 ${sizeClass} ${animation}`, attrs: { 'aria-hidden': 'true' } }, [
    h('span', { class: `absolute left-1/2 top-0 -translate-x-1/2 -translate-y-1/2 rounded-full ${dotClass}` }),
  ]);

/** Orbital "thinking" animation while the AI classifies the note. */
export class ProcessingView implements WatchView {
  private readonly transcript = h('p', { class: 'line-clamp-2 max-w-[220px] text-xs leading-5 text-muted' });

  readonly element = h('div', { class: VIEW_BASE_CLASS, attrs: { role: 'status' } }, [
    h('div', { class: 'relative grid h-28 w-28 place-items-center' }, [
      orbit('h-28 w-28', 'animate-spin-slow', 'h-3 w-3 bg-accent shadow-[0_0_12px_rgb(var(--color-accent))]'),
      orbit('h-20 w-20', 'animate-[spin_1.6s_linear_infinite_reverse]', 'h-2.5 w-2.5 bg-primary'),
      h('div', { class: 'grid h-12 w-12 place-items-center rounded-full bg-primary/15 text-primary' }, [icon(Sparkles, 24)]),
    ]),
    h('p', { class: 'text-sm font-semibold text-fg' }, [t('watch.processing')]),
    this.transcript,
  ]);

  update(snapshot: WatchSnapshot): void {
    this.transcript.textContent = `“${snapshot.transcript}”`;
  }

  destroy(): void {
    this.element.remove();
  }
}
