/** Contract for every UI component: an element plus a way to release resources. */
export interface Component {
  readonly element: HTMLElement;
  destroy(): void;
}
