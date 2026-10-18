import { Square } from 'lucide';
import { h } from '../../../../shared/dom/h';
import { icon } from '../../../../shared/dom/icon';
import { t } from '../../../../shared/i18n';
import { WaveformCanvas } from '../../../capture/components/WaveformCanvas';
import { AudioLevelMeter, createSyntheticLevels } from '../../../capture/services/levelSources';
import type { WatchSnapshot } from '../../models/watchTypes';
import { VIEW_BASE_CLASS, type WatchView, type WatchViewContext } from './WatchView';

const WAVE_WIDTH = 200;
const WAVE_HEIGHT = 64;

/** Real mic levels only on desktop: mobile browsers let speech recognition own the mic. */
const canReadMicLevels = () => window.matchMedia?.('(pointer: fine)').matches ?? false;

export class ListeningView implements WatchView {
  readonly element: HTMLElement;
  private readonly waveform = new WaveformCanvas(WAVE_WIDTH, WAVE_HEIGHT, t('watch.listening'));
  private readonly synthetic = createSyntheticLevels();
  private meter: AudioLevelMeter | null = null;
  private readonly transcript = h('p', {
    class: 'line-clamp-2 min-h-[2.5rem] max-w-[230px] text-sm leading-5 text-fg',
    attrs: { 'aria-live': 'polite' },
  });
  private lastInterim = '';
  private destroyed = false;

  constructor(
    context: WatchViewContext,
    simulated: boolean
  ) {
    this.element = h('div', { class: VIEW_BASE_CLASS }, [
      h('p', { class: 'text-xs font-semibold uppercase tracking-wider text-accent' }, [t('watch.listening')]),
      this.waveform.element,
      this.transcript,
      h(
        'button',
        {
          class: 'grid h-14 w-14 place-items-center rounded-full bg-danger text-on-primary shadow-raised transition-transform active:scale-95',
          attrs: { type: 'button', 'aria-label': t('watch.stopListening') },
          on: { click: () => context.dispatch({ type: 'primary' }) },
        },
        [icon(Square, 20, 'fill-current')]
      ),
    ]);
    this.waveform.start(this.synthetic.next);
    if (!simulated && canReadMicLevels()) void this.attachMeter();
  }

  update(snapshot: WatchSnapshot): void {
    if (snapshot.interim === this.lastInterim) return;
    this.lastInterim = snapshot.interim;
    this.transcript.textContent = snapshot.interim;
    this.synthetic.bump();
  }

  destroy(): void {
    this.destroyed = true;
    this.waveform.stop();
    this.meter?.stop();
    this.element.remove();
  }

  private async attachMeter(): Promise<void> {
    const meter = new AudioLevelMeter();
    const started = await meter.start();
    if (this.destroyed) {
      meter.stop();
      return;
    }
    if (!started) return;
    this.meter = meter;
    this.waveform.start(() => Math.max(meter.level(), 0.08));
  }
}
