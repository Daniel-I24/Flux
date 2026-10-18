import { setAttributes } from './h';

const SVG_NS = 'http://www.w3.org/2000/svg';

/** SVG counterpart of `h()` for drawn UI such as progress rings. */
export const svg = <K extends keyof SVGElementTagNameMap>(
  tag: K,
  attrs: Record<string, string | number | boolean | undefined> = {},
  children: SVGElement[] = []
): SVGElementTagNameMap[K] => {
  const element = document.createElementNS(SVG_NS, tag);
  setAttributes(element, attrs);
  children.forEach((child) => element.appendChild(child));
  return element;
};
