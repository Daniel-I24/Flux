import type { ScaledBox } from './ScaledBox';

/** Keeps a ScaledBox as large as its container allows (between `min` and `max` scale). */
export const fitToContainer = (box: ScaledBox, container: HTMLElement, min = 0.4, max = 1.6): (() => void) => {
  const layout = () => {
    const style = getComputedStyle(container);
    const width = container.clientWidth - parseFloat(style.paddingLeft) - parseFloat(style.paddingRight);
    const height = container.clientHeight - parseFloat(style.paddingTop) - parseFloat(style.paddingBottom);
    if (width <= 0 || height <= 0) return;
    const scale = Math.min(width / box.size.width, height / box.size.height);
    box.setScale(Math.min(max, Math.max(min, scale)));
  };
  const observer = new ResizeObserver(layout);
  observer.observe(container);
  layout();
  return () => observer.disconnect();
};
