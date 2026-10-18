import type { IconNode } from 'lucide';
import { h } from '../dom/h';
import { icon as renderIcon } from '../dom/icon';

export type ButtonVariant = 'primary' | 'secondary' | 'ghost' | 'danger';
export type ButtonSize = 'sm' | 'md' | 'lg';

export interface ButtonOptions {
  label: string;
  variant?: ButtonVariant;
  size?: ButtonSize;
  icon?: IconNode;
  /** Renders only the icon; `label` becomes the accessible name and tooltip. */
  iconOnly?: boolean;
  type?: 'button' | 'submit';
  className?: string;
  disabled?: boolean;
  pressed?: boolean;
  onClick?: (event: MouseEvent) => void;
}

const BASE =
  'inline-flex select-none items-center justify-center gap-2 rounded-xl font-medium ' +
  'transition-colors duration-150 disabled:pointer-events-none disabled:opacity-50';

const VARIANTS: Record<ButtonVariant, string> = {
  primary: 'bg-primary text-on-primary shadow-sm hover:bg-primary/90',
  secondary: 'border border-border bg-surface-2 text-fg hover:bg-border/70',
  ghost: 'text-fg hover:bg-surface-2',
  danger: 'bg-danger text-on-primary hover:bg-danger/90',
};

const SIZES: Record<ButtonSize, { text: string; icon: string; iconPx: number }> = {
  sm: { text: 'h-9 px-3 text-sm', icon: 'h-9 w-9', iconPx: 16 },
  md: { text: 'h-11 px-4 text-sm', icon: 'h-11 w-11', iconPx: 18 },
  lg: { text: 'h-12 px-5 text-base', icon: 'h-14 w-14', iconPx: 24 },
};

export const createButton = (options: ButtonOptions): HTMLButtonElement => {
  const variant = options.variant ?? 'secondary';
  const size = SIZES[options.size ?? 'md'];
  const shape = options.iconOnly ? `${size.icon} rounded-full` : size.text;
  return h(
    'button',
    {
      class: `${BASE} ${VARIANTS[variant]} ${shape} ${options.className ?? ''}`.trim(),
      attrs: {
        type: options.type ?? 'button',
        disabled: options.disabled,
        'aria-label': options.iconOnly ? options.label : undefined,
        title: options.iconOnly ? options.label : undefined,
        'aria-pressed': options.pressed === undefined ? undefined : String(options.pressed),
      },
      on: options.onClick ? { click: options.onClick } : {},
    },
    [
      options.icon ? renderIcon(options.icon, size.iconPx) : null,
      options.iconOnly ? null : h('span', { class: 'truncate' }, [options.label]),
    ]
  );
};
