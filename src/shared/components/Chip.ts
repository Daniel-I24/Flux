import { h } from '../dom/h';

/** Full Tailwind class names (kept literal so the JIT compiler can see them). */
export interface ChipTone {
  text: string;
  soft: string;
  dot: string;
}

export const PRIMARY_TONE: ChipTone = { text: 'text-primary', soft: 'bg-primary/10', dot: 'bg-primary' };

export interface ChipOptions {
  label: string;
  count?: number;
  pressed: boolean;
  tone?: ChipTone;
  showDot?: boolean;
  onClick: () => void;
}

/** Toggle chip with aria-pressed, used for category filters. */
export const createChip = (options: ChipOptions): HTMLButtonElement => {
  const tone = options.tone ?? PRIMARY_TONE;
  const state = options.pressed
    ? `border-transparent ${tone.soft} text-fg ring-1 ring-inset ring-current ${tone.text}`
    : 'border-border bg-surface text-muted hover:text-fg';
  return h(
    'button',
    {
      class:
        'inline-flex h-11 shrink-0 items-center gap-2 rounded-full border px-4 text-sm font-medium ' +
        `transition-colors duration-150 ${state}`,
      attrs: { type: 'button', 'aria-pressed': String(options.pressed) },
      on: { click: options.onClick },
    },
    [
      options.showDot ? h('span', { class: `h-2 w-2 rounded-full ${tone.dot}`, attrs: { 'aria-hidden': 'true' } }) : null,
      h('span', { class: 'text-fg' }, [options.label]),
      options.count === undefined
        ? null
        : h('span', { class: 'rounded-full bg-surface-2 px-2 py-0.5 text-xs tabular-nums text-fg' }, [options.count]),
    ]
  );
};
