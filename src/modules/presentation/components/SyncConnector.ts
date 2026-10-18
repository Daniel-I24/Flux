import { ArrowLeftRight } from 'lucide';
import { APP_CONFIG } from '../../../core/config/appConfig';
import { appEvents } from '../../../core/events/appEvents';
import type { Unsubscribe } from '../../../core/types/domain';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';

/** Animated link between the two devices that lights up on every sync message. */
export class SyncConnector {
  readonly element: HTMLElement;
  private readonly pulse: HTMLElement;
  private readonly track: HTMLElement;
  private readonly unsubscribe: Unsubscribe;
  private resetTimer: ReturnType<typeof setTimeout> | null = null;
  private vertical = false;

  constructor() {
    this.pulse = h('span', { class: 'absolute inset-0 rounded-full bg-gradient-to-r from-transparent via-accent to-transparent opacity-0' });
    this.track = h('div', { class: 'relative overflow-hidden rounded-full bg-border transition-colors duration-300' }, [this.pulse]);
    this.element = h('div', { class: 'relative flex shrink-0 items-center justify-center', attrs: { role: 'img', 'aria-label': t('demo.connector') } }, [
      this.track,
      h('span', { class: 'absolute grid h-10 w-10 place-items-center rounded-full border border-border bg-surface text-muted shadow-card transition-colors duration-300' }, [
        icon(ArrowLeftRight, 18),
      ]),
    ]);
    this.setOrientation(false);
    this.unsubscribe = appEvents.on('sync:activity', () => this.flash());
  }

  setOrientation(vertical: boolean): void {
    this.vertical = vertical;
    this.element.style.width = vertical ? '40px' : '120px';
    this.element.style.height = vertical ? '72px' : '40px';
    this.track.className = `relative overflow-hidden rounded-full bg-border transition-colors duration-300 ${vertical ? 'h-full w-1' : 'h-1 w-full'}`;
    this.element.querySelector('svg')?.classList.toggle('rotate-90', vertical);
  }

  destroy(): void {
    this.unsubscribe();
    if (this.resetTimer) clearTimeout(this.resetTimer);
    this.element.remove();
  }

  private flash(): void {
    const badge = this.element.querySelector('span.absolute.grid');
    this.track.classList.add('bg-accent/40');
    badge?.classList.add('text-accent', 'border-accent');
    this.pulse.classList.remove('animate-flow', 'animate-flow-y', 'opacity-0');
    void this.pulse.offsetWidth;
    this.pulse.classList.add(this.vertical ? 'animate-flow-y' : 'animate-flow');
    if (this.resetTimer) clearTimeout(this.resetTimer);
    this.resetTimer = setTimeout(() => {
      this.track.classList.remove('bg-accent/40');
      badge?.classList.remove('text-accent', 'border-accent');
      this.pulse.classList.add('opacity-0');
    }, APP_CONFIG.sync.activityPulseMs);
  }
}
