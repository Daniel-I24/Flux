import { BatteryFull, SignalHigh, Wifi } from 'lucide';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { formatClock } from '../../../shared/utils/dateUtils';

const SCREEN = { width: 390, height: 800 };
const BEZEL = 12;

export const PHONE_FRAME_SIZE = { width: SCREEN.width + BEZEL * 2, height: SCREEN.height + BEZEL * 2 };

export interface PhoneFrameHandle {
  element: HTMLElement;
  destroy(): void;
}

/** Decorative Android hardware: bezel, punch-hole camera, status bar and side keys. */
export const createPhoneFrame = (screen: HTMLElement, languageTag: string, label: string): PhoneFrameHandle => {
  const clock = h('span', { class: 'tabular-nums' });
  const tick = () => (clock.textContent = formatClock(new Date(), languageTag));
  tick();
  const timer = setInterval(tick, 10_000);

  const element = h(
    'div',
    {
      class: 'relative rounded-[52px] bg-gradient-to-b from-slate-700 to-slate-900 shadow-device',
      style: { width: `${PHONE_FRAME_SIZE.width}px`, height: `${PHONE_FRAME_SIZE.height}px`, padding: `${BEZEL}px` },
    },
    [
      h('span', { class: 'absolute -left-[3px] top-36 h-16 w-[3px] rounded-l bg-slate-600', attrs: { 'aria-hidden': 'true' } }),
      h('span', { class: 'absolute -right-[3px] top-28 h-24 w-[3px] rounded-r bg-slate-600', attrs: { 'aria-hidden': 'true' } }),
      h(
        'section',
        {
          class: 'relative flex h-full w-full flex-col overflow-hidden rounded-[40px] bg-bg',
          attrs: { 'aria-label': label },
        },
        [
          h('div', { class: 'relative flex h-8 shrink-0 items-center justify-between px-7 text-xs font-semibold text-fg', attrs: { 'aria-hidden': 'true' } }, [
            clock,
            h('span', { class: 'absolute left-1/2 top-2 h-4 w-4 -translate-x-1/2 rounded-full bg-black ring-2 ring-slate-800' }),
            h('span', { class: 'flex items-center gap-1' }, [icon(SignalHigh, 14), icon(Wifi, 14), icon(BatteryFull, 16)]),
          ]),
          h('div', { class: 'min-h-0 flex-1' }, [screen]),
        ]
      ),
    ]
  );

  return {
    element,
    destroy: () => {
      clearInterval(timer);
      element.remove();
    },
  };
};
