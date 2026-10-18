/**
 * Tiny DOM builder. Strings always become text nodes, so user content can never
 * be interpreted as HTML (XSS-safe by construction; innerHTML is banned by lint).
 */
export type Child = Node | string | number | null | undefined | false;
type AttrValue = string | number | boolean | null | undefined;
type Listeners = { [K in keyof HTMLElementEventMap]?: (event: HTMLElementEventMap[K]) => void };

export interface ElementProps {
  class?: string;
  attrs?: Record<string, AttrValue>;
  on?: Listeners;
  style?: Partial<Record<string, string>>;
}

export const setAttributes = (element: Element, attrs: Record<string, AttrValue>): void => {
  Object.entries(attrs).forEach(([name, value]) => {
    if (value === false || value === null || value === undefined) element.removeAttribute(name);
    else element.setAttribute(name, value === true ? '' : String(value));
  });
};

export const toNode = (child: Exclude<Child, null | undefined | false>): Node =>
  child instanceof Node ? child : document.createTextNode(String(child));

export const appendChildren = (parent: Node, children: Child[]): void => {
  children.forEach((child) => {
    if (child === null || child === undefined || child === false) return;
    parent.appendChild(toNode(child));
  });
};

export function h<K extends keyof HTMLElementTagNameMap>(
  tag: K,
  props: ElementProps = {},
  children: Child[] = []
): HTMLElementTagNameMap[K] {
  const element = document.createElement(tag);
  if (props.class) element.className = props.class;
  if (props.attrs) setAttributes(element, props.attrs);
  if (props.style) {
    Object.entries(props.style).forEach(([name, value]) => {
      if (value !== undefined) element.style.setProperty(name, value);
    });
  }
  if (props.on) {
    Object.entries(props.on).forEach(([type, listener]) => {
      element.addEventListener(type, listener as EventListener);
    });
  }
  appendChildren(element, children);
  return element;
}

export const replaceContent = (parent: Element, children: Child[]): void => {
  parent.replaceChildren();
  appendChildren(parent, children);
};
