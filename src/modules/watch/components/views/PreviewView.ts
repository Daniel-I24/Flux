import { CalendarClock, Check, Pencil, X } from 'lucide';
import { createButton } from '../../../../shared/components/Button';
import { h, replaceContent } from '../../../../shared/dom/h';
import { icon } from '../../../../shared/dom/icon';
import { svg } from '../../../../shared/dom/svg';
import { t } from '../../../../shared/i18n';
import { formatDueDate, parseIsoDate } from '../../../../shared/utils/dateUtils';
import { TextCaptureForm } from '../../../capture/components/TextCaptureForm';
import { createCategoryBadge } from '../../../notes/components/CategoryBadge';
import type { WatchSnapshot } from '../../models/watchTypes';
import { VIEW_BASE_CLASS, type WatchView, type WatchViewContext } from './WatchView';

const RING_RADIUS = 172;
const RING_LENGTH = 2 * Math.PI * RING_RADIUS;

/** Classified note preview with an auto-save ring that follows the round screen edge. */
export class PreviewView implements WatchView {
  readonly element = h('div', { class: VIEW_BASE_CLASS });
  readonly overlay: SVGSVGElement;
  private readonly ring: SVGCircleElement;
  private readonly countdownLabel = h('p', { class: 'text-[11px] font-medium tabular-nums text-muted', attrs: { 'aria-live': 'polite' } });
  private renderedKey = '';

  constructor(private readonly context: WatchViewContext) {
    this.ring = svg('circle', {
      cx: 180, cy: 180, r: RING_RADIUS, fill: 'none', 'stroke-width': 6, 'stroke-linecap': 'round',
      class: 'stroke-primary transition-[stroke-dashoffset] duration-1000 ease-linear',
      'stroke-dasharray': RING_LENGTH, 'stroke-dashoffset': 0,
    });
    this.overlay = svg('svg', { viewBox: '0 0 360 360', class: 'absolute inset-0 h-full w-full -rotate-90', 'aria-hidden': 'true' }, [
      svg('circle', { cx: 180, cy: 180, r: RING_RADIUS, fill: 'none', 'stroke-width': 6, class: 'stroke-border/60' }),
      this.ring,
    ]);
  }

  update(snapshot: WatchSnapshot): void {
    const key = `${snapshot.editing}|${snapshot.result?.title}|${snapshot.result?.dueAt}`;
    if (key !== this.renderedKey) {
      this.renderedKey = key;
      if (snapshot.editing) this.renderEditor(snapshot);
      else this.renderPreview(snapshot);
    }
    this.updateCountdown(snapshot);
  }

  destroy(): void {
    this.overlay.remove();
    this.element.remove();
  }

  private updateCountdown(snapshot: WatchSnapshot): void {
    const total = this.context.autoSaveSeconds;
    const progress = snapshot.editing ? 1 : Math.max(0, snapshot.secondsLeft) / total;
    this.ring.setAttribute('stroke-dashoffset', String(RING_LENGTH * (1 - progress)));
    this.overlay.classList.toggle('opacity-30', snapshot.editing);
    this.countdownLabel.textContent = snapshot.editing ? '' : t('watch.autoSaveIn', { seconds: Math.max(0, snapshot.secondsLeft) });
  }

  private renderPreview(snapshot: WatchSnapshot): void {
    const result = snapshot.result;
    if (!result) return;
    const { dispatch, languageTag } = this.context;
    const due = parseIsoDate(result.dueAt);
    replaceContent(this.element, [
      createCategoryBadge(result.category, 'md'),
      h('p', { class: 'line-clamp-3 max-w-[236px] text-base font-semibold leading-snug text-fg' }, [result.title]),
      h('p', { class: `flex items-center gap-1.5 text-xs font-medium ${due ? 'text-accent' : 'text-muted'}` }, [
        icon(CalendarClock, 14),
        due ? formatDueDate(due, new Date(), languageTag) : t('time.noDate'),
      ]),
      h('div', { class: 'mt-1 flex items-center gap-3' }, [
        createButton({ label: t('actions.cancel'), icon: X, iconOnly: true, onClick: () => dispatch({ type: 'cancel' }) }),
        createButton({ label: t('actions.save'), icon: Check, iconOnly: true, variant: 'primary', size: 'lg', onClick: () => dispatch({ type: 'confirm' }) }),
        createButton({ label: t('actions.edit'), icon: Pencil, iconOnly: true, onClick: () => dispatch({ type: 'edit' }) }),
      ]),
      this.countdownLabel,
    ]);
  }

  private renderEditor(snapshot: WatchSnapshot): void {
    const { dispatch, maxLength } = this.context;
    const form = new TextCaptureForm({
      label: t('watch.editTitle'),
      placeholder: t('watch.typePlaceholder'),
      submitLabel: t('watch.submitText'),
      maxLength,
      variant: 'watch',
      initialValue: snapshot.transcript,
      onSubmit: (text) => dispatch({ type: 'resubmit', text }),
    });
    replaceContent(this.element, [
      h('p', { class: 'text-sm font-semibold text-fg' }, [t('watch.editTitle')]),
      h('div', { class: 'w-[236px]' }, [form.element]),
      createButton({ label: t('actions.cancel'), icon: X, iconOnly: true, variant: 'ghost', onClick: () => dispatch({ type: 'cancelEdit' }) }),
    ]);
    queueMicrotask(() => form.focus());
  }
}
