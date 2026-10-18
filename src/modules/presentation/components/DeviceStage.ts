import { ScaledBox } from '../../../shared/components/ScaledBox';
import { h } from '../../../shared/dom/h';
import type { SyncConnector } from './SyncConnector';

const ROW_BREAKPOINT = 880;
const PADDING = 24;
const GAP = 24;
const MIN_SCALE = 0.4;
const MAX_SCALE = 1.2;

const clamp = (value: number) => Math.min(MAX_SCALE, Math.max(MIN_SCALE, value));

/**
 * Centers watch and phone side by side (stacked on narrow screens) and scales
 * both proportionally to the available viewport, e.g. 1366x768 and 1920x1080.
 */
export class DeviceStage {
  readonly element: HTMLElement;
  private readonly observer: ResizeObserver;

  constructor(
    private readonly watch: ScaledBox,
    private readonly phone: ScaledBox,
    private readonly connector: SyncConnector,
    private readonly win: Window = window
  ) {
    this.element = h('div', { class: 'flex w-full flex-1 items-center justify-center', style: { padding: `${PADDING}px`, gap: `${GAP}px` } }, [
      watch.element,
      connector.element,
      phone.element,
    ]);
    this.observer = new ResizeObserver(() => this.layout());
    this.observer.observe(this.element);
    this.win.addEventListener('resize', this.layout);
  }

  destroy(): void {
    this.observer.disconnect();
    this.win.removeEventListener('resize', this.layout);
    this.connector.destroy();
    this.element.remove();
  }

  private readonly layout = () => {
    const availableWidth = this.element.clientWidth - PADDING * 2;
    const isRow = this.win.innerWidth >= ROW_BREAKPOINT;
    this.element.classList.toggle('flex-col', !isRow);
    this.connector.setOrientation(!isRow);

    if (isRow) {
      const top = this.element.getBoundingClientRect().top + this.win.scrollY;
      const availableHeight = this.win.innerHeight - top - PADDING * 2;
      const neededWidth = this.watch.size.width + this.phone.size.width + 120 + GAP * 2;
      const neededHeight = Math.max(this.watch.size.height, this.phone.size.height);
      const scale = clamp(Math.min(availableWidth / neededWidth, availableHeight / neededHeight));
      this.watch.setScale(scale);
      this.phone.setScale(scale);
      return;
    }
    this.watch.setScale(clamp(Math.min(1, availableWidth / this.watch.size.width)));
    this.phone.setScale(clamp(Math.min(1, availableWidth / this.phone.size.width)));
  };
}
