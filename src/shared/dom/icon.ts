import { createElement, type IconNode } from 'lucide';

const BASE_ATTRS = {
  xmlns: 'http://www.w3.org/2000/svg',
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  'stroke-linecap': 'round',
  'stroke-linejoin': 'round',
  'aria-hidden': 'true',
  focusable: 'false',
};

/** Renders a decorative lucide icon (always hidden from assistive tech). */
export const icon = (node: IconNode, size = 20, className = '', strokeWidth = 2): SVGElement =>
  createElement([
    'svg',
    {
      ...BASE_ATTRS,
      width: size,
      height: size,
      'stroke-width': strokeWidth,
      class: `shrink-0 ${className}`.trim(),
    },
    node,
  ]);
