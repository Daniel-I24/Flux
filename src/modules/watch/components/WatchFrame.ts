import { h } from '../../../shared/dom/h';
import { WATCH_SCREEN_SIZE } from './WatchFace';

const BEZEL = 22;
const STRAP_HEIGHT = 64;

export const WATCH_FRAME_SIZE = {
  width: WATCH_SCREEN_SIZE + BEZEL * 2 + 16,
  height: WATCH_SCREEN_SIZE + BEZEL * 2 + STRAP_HEIGHT * 2,
};

const strap = (position: 'top' | 'bottom') =>
  h('div', {
    class:
      'absolute left-[112px] w-[180px] bg-gradient-to-b from-slate-700 to-slate-900 ' +
      (position === 'top' ? 'top-0 rounded-t-[28px]' : 'bottom-0 rounded-b-[28px]'),
    style: { height: `${STRAP_HEIGHT + BEZEL}px` },
    attrs: { 'aria-hidden': 'true' },
  });

/** Decorative Wear OS style hardware: metallic bezel, crown button and short straps. */
export const createWatchFrame = (screen: HTMLElement): HTMLElement =>
  h(
    'div',
    {
      class: 'relative',
      style: { width: `${WATCH_FRAME_SIZE.width}px`, height: `${WATCH_FRAME_SIZE.height}px` },
    },
    [
      strap('top'),
      strap('bottom'),
      h('div', {
        class: 'absolute right-0 top-1/2 h-12 w-4 -translate-y-1/2 rounded-r-lg bg-gradient-to-r from-slate-500 to-slate-700',
        attrs: { 'aria-hidden': 'true' },
      }),
      h(
        'div',
        {
          class:
            'absolute left-0 top-1/2 grid -translate-y-1/2 place-items-center rounded-full ' +
            'bg-gradient-to-br from-slate-500 via-slate-800 to-slate-950 shadow-device',
          style: { width: `${WATCH_SCREEN_SIZE + BEZEL * 2}px`, height: `${WATCH_SCREEN_SIZE + BEZEL * 2}px` },
        },
        [h('div', { class: 'rounded-full bg-black p-1.5' }, [screen])]
      ),
    ]
  );
