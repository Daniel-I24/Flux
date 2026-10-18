import { h } from '../dom/h';

export interface Size {
  width: number;
  height: number;
}

/**
 * Renders fixed-size content (a device mock-up) at any scale while keeping the
 * layout box exact, so nothing overflows or causes horizontal scroll.
 */
export class ScaledBox {
  readonly element: HTMLElement;
  private readonly inner: HTMLElement;

  constructor(content: HTMLElement, readonly size: Size) {
    this.inner = h('div', { class: 'absolute left-0 top-0 origin-top-left', style: { width: `${size.width}px`, height: `${size.height}px` } }, [content]);
    this.element = h('div', { class: 'relative shrink-0' }, [this.inner]);
    this.setScale(1);
  }

  setScale(scale: number): void {
    this.element.style.width = `${this.size.width * scale}px`;
    this.element.style.height = `${this.size.height * scale}px`;
    this.inner.style.transform = `scale(${scale})`;
  }
}
