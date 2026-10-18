import { AudioWaveform, RotateCcw, Smartphone, Watch } from 'lucide';
import { appEvents } from '../../../core/events/appEvents';
import type { CaptureMode, Unsubscribe } from '../../../core/types/domain';
import { createButton } from '../../../shared/components/Button';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';
import { DEMO_CASES } from '../models/demoCases';
import { createLanguageToggle, createMicToggle, createThemeToggle, renderAiStatus } from './toolbarControls';

/** What the toolbar needs from the demo page (no dependency on the composition root). */
export interface DemoTargets {
  simulate(phrase: string): void;
  resetNotes(): Promise<void>;
  captureMode: { readonly current: CaptureMode; toggle(): void };
  remoteAiAvailable(): boolean;
  navigate(path: string): void;
}

/** Top bar: brand, five one-click cases, mic switch, AI status, language, theme and views. */
export class DemoToolbar {
  readonly element: HTMLElement;
  private readonly aiStatus = h('span', { attrs: { role: 'status' } });
  private readonly micSlot = h('span', { class: 'contents' });
  private readonly subscriptions: Unsubscribe[];

  constructor(private readonly targets: DemoTargets) {
    const cases = DEMO_CASES.map((demoCase) =>
      createButton({
        label: t(`demo.cases.${demoCase.id}`),
        icon: demoCase.icon,
        size: 'sm',
        className: 'shrink-0',
        onClick: () => targets.simulate(demoCase.phrase),
      })
    );
    cases.forEach((button, index) => button.setAttribute('title', DEMO_CASES[index]?.phrase ?? ''));

    const navLink = (path: string, label: string, node: typeof Watch) =>
      createButton({ label, icon: node, iconOnly: true, size: 'sm', variant: 'ghost', onClick: () => targets.navigate(path) });

    this.element = h('header', { class: 'sticky top-0 z-40 border-b border-border bg-surface/90 backdrop-blur' }, [
      h('div', { class: 'mx-auto flex max-w-[1600px] flex-wrap items-center gap-x-4 gap-y-2 px-4 py-2' }, [
        h('div', { class: 'flex items-center gap-2' }, [
          h('span', { class: 'grid h-9 w-9 place-items-center rounded-xl bg-gradient-to-br from-primary to-accent text-on-primary' }, [icon(AudioWaveform, 20, '', 2.4)]),
          h('div', { class: 'leading-tight' }, [
            h('p', { class: 'text-base font-bold tracking-tight text-fg' }, [t('app.name')]),
            h('p', { class: 'hidden text-[11px] text-muted sm:block' }, [t('demo.title')]),
          ]),
        ]),
        h('nav', { class: 'order-3 flex w-full items-center gap-2 overflow-x-auto pb-1 lg:order-none lg:w-auto lg:flex-1 lg:justify-center lg:pb-0', attrs: { 'aria-label': t('demo.casesLabel') } }, [
          h('span', { class: 'hidden shrink-0 text-xs font-semibold uppercase tracking-wide text-muted xl:inline' }, [t('demo.casesLabel')]),
          ...cases,
        ]),
        h('div', { class: 'ml-auto flex items-center gap-1' }, [
          this.micSlot,
          this.aiStatus,
          createLanguageToggle(),
          createThemeToggle(),
          createButton({ label: t('demo.reset'), icon: RotateCcw, iconOnly: true, size: 'sm', variant: 'ghost', onClick: () => void targets.resetNotes() }),
          h('span', { class: 'mx-1 hidden h-6 w-px bg-border sm:block', attrs: { 'aria-hidden': 'true' } }),
          h('span', { class: 'hidden sm:contents', attrs: { 'aria-label': t('demo.navLabel') } }, [
            navLink('/watch', t('demo.openWatch'), Watch),
            navLink('/phone', t('demo.openPhone'), Smartphone),
          ]),
        ]),
      ]),
    ]);

    this.renderMic();
    renderAiStatus(this.aiStatus, targets.remoteAiAvailable());
    this.subscriptions = [
      appEvents.on('ai:status-changed', ({ remoteAvailable }) => renderAiStatus(this.aiStatus, remoteAvailable)),
      appEvents.on('capture:mode-changed', () => this.renderMic()),
    ];
  }

  destroy(): void {
    this.subscriptions.forEach((unsubscribe) => unsubscribe());
    this.element.remove();
  }

  private renderMic(): void {
    const hadFocus = this.micSlot.contains(document.activeElement);
    const button = createMicToggle(this.targets.captureMode.current, () => this.targets.captureMode.toggle());
    this.micSlot.replaceChildren(button);
    if (hadFocus) button.focus();
  }
}
