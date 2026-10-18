import { describe, expect, it, vi } from 'vitest';
import { Router, type ViewFactory } from './Router';

const flush = () => new Promise((resolve) => setTimeout(resolve, 0));

const createView = (label: string, destroy = vi.fn()): ViewFactory => (outlet) => {
  outlet.textContent = label;
  return { destroy };
};

describe('Router', () => {
  it('redirects unknown paths to the fallback route', async () => {
    window.history.replaceState(null, '', '/unknown');
    const outlet = document.createElement('main');
    const router = new Router(outlet, [{ path: '/demo', load: async () => createView('demo') }], '/demo');

    router.start();
    await flush();

    expect(window.location.pathname).toBe('/demo');
    expect(outlet.textContent).toBe('demo');
  });

  it('destroys the previous view before mounting the next one', async () => {
    window.history.replaceState(null, '', '/watch');
    const outlet = document.createElement('main');
    const destroyWatch = vi.fn();
    const router = new Router(
      outlet,
      [
        { path: '/watch', load: async () => createView('watch', destroyWatch) },
        { path: '/phone', load: async () => createView('phone') },
      ],
      '/watch'
    );

    router.start();
    await flush();
    router.navigate('/phone?pair=ABC123');
    await flush();

    expect(destroyWatch).toHaveBeenCalledTimes(1);
    expect(outlet.textContent).toBe('phone');
    expect(window.location.search).toBe('?pair=ABC123');
  });

  it('reports loader failures through the error hook', async () => {
    window.history.replaceState(null, '', '/demo');
    const onError = vi.fn();
    const router = new Router(
      document.createElement('main'),
      [{ path: '/demo', load: () => Promise.reject(new Error('chunk failed')) }],
      '/demo',
      { onError }
    );

    router.start();
    await flush();

    expect(onError).toHaveBeenCalledWith(expect.any(HTMLElement), expect.any(Error), expect.any(Function));
  });
});
