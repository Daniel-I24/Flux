import { h } from '../../../shared/dom/h';
import type { LevelSource } from '../services/levelSources';

const BAR_COUNT = 21;
const BAR_GAP_RATIO = 0.45;
const MIN_BAR = 0.08;
const SMOOTHING = 0.35;

const prefersReducedMotion = () => window.matchMedia?.('(prefers-reduced-motion: reduce)').matches ?? false;

/** Animated audio bars drawn on a canvas; releases its animation frame on stop(). */
export class WaveformCanvas {
  readonly element: HTMLCanvasElement;
  private frame: number | null = null;
  private levels: number[] = Array.from({ length: BAR_COUNT }, () => MIN_BAR);

  constructor(
    private readonly width: number,
    private readonly height: number,
    label: string
  ) {
    this.element = h('canvas', {
      class: 'block',
      attrs: { role: 'img', 'aria-label': label },
      style: { width: `${width}px`, height: `${height}px` },
    });
    const ratio = window.devicePixelRatio || 1;
    this.element.width = Math.round(width * ratio);
    this.element.height = Math.round(height * ratio);
  }

  start(source: LevelSource): void {
    this.stop();
    if (prefersReducedMotion()) {
      this.levels = this.levels.map((_, index) => 0.3 + 0.3 * Math.sin((index / BAR_COUNT) * Math.PI));
      this.draw();
      return;
    }
    const tick = () => {
      this.push(source());
      this.draw();
      this.frame = requestAnimationFrame(tick);
    };
    this.frame = requestAnimationFrame(tick);
  }

  stop(): void {
    if (this.frame !== null) cancelAnimationFrame(this.frame);
    this.frame = null;
  }

  private push(level: number): void {
    const center = Math.floor(BAR_COUNT / 2);
    this.levels = this.levels.map((current, index) => {
      const falloff = 1 - Math.abs(index - center) / (center + 1);
      const jitter = 0.75 + Math.random() * 0.5;
      const target = Math.max(MIN_BAR, level * falloff * jitter);
      return current + (target - current) * SMOOTHING;
    });
  }

  private draw(): void {
    const context = this.element.getContext('2d');
    if (!context) return;
    const ratio = this.element.width / this.width;
    const color = getComputedStyle(this.element).getPropertyValue('--color-accent').trim() || '34 211 238';
    context.setTransform(ratio, 0, 0, ratio, 0, 0);
    context.clearRect(0, 0, this.width, this.height);
    context.fillStyle = `rgb(${color.split(' ').join(',')})`;
    const slot = this.width / BAR_COUNT;
    const barWidth = slot * (1 - BAR_GAP_RATIO);
    this.levels.forEach((level, index) => {
      const barHeight = Math.max(barWidth, level * this.height);
      const x = index * slot + (slot - barWidth) / 2;
      const y = (this.height - barHeight) / 2;
      context.beginPath();
      context.roundRect(x, y, barWidth, barHeight, barWidth / 2);
      context.fill();
    });
  }
}
