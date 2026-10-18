import type { RouteDefinition } from '../core/router/Router';

/**
 * Route registry. Adding a view means adding one entry here; dynamic imports
 * keep every page in its own chunk.
 */
export const ROUTES: RouteDefinition[] = [
  { path: '/demo', load: () => import('./pages/DemoPage').then((page) => page.createDemoPage) },
  { path: '/watch', load: () => import('./pages/WatchPage').then((page) => page.createWatchPage) },
  { path: '/phone', load: () => import('./pages/PhonePage').then((page) => page.createPhonePage) },
];

export const DEFAULT_ROUTE = '/demo';
