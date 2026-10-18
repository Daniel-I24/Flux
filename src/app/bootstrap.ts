import { CircleAlert } from 'lucide';
import { AppError } from '../core/errors/AppError';
import { appEvents } from '../core/events/appEvents';
import { Router } from '../core/router/Router';
import { createButton } from '../shared/components/Button';
import { createEmptyState, createSpinner } from '../shared/components/Feedback';
import { h, replaceContent } from '../shared/dom/h';
import { i18n, t } from '../shared/i18n';
import { themeService } from '../shared/theme';
import { audioSynthesizer } from '../shared/utils/audioSynthesizer';
import { DEFAULT_ROUTE, ROUTES } from './routes';

const renderLoading = (outlet: HTMLElement) =>
  replaceContent(outlet, [h('div', { class: 'grid min-h-dvh place-items-center' }, [createSpinner(t('app.loading'))])]);

const renderError = (outlet: HTMLElement, _error: unknown, retry: () => void) =>
  replaceContent(outlet, [
    h('div', { class: 'grid min-h-dvh place-items-center' }, [
      createEmptyState({
        icon: CircleAlert,
        tone: 'danger',
        title: t('app.routeError'),
        action: createButton({ label: t('actions.retry'), variant: 'primary', onClick: retry }),
      }),
    ]),
  ]);

/** Composition root: applies global settings and starts the router. */
export const bootstrap = (root: HTMLElement | null): void => {
  if (!root) throw new AppError('Missing #app root element', 'NOT_FOUND');

  themeService.apply();
  document.documentElement.lang = i18n.current;

  const outlet = h('main', { class: 'min-h-dvh', attrs: { id: 'main-content', tabindex: '-1' } });
  const skipLink = h(
    'a',
    {
      class: 'sr-only-focusable fixed left-4 top-4 z-[100] rounded-xl bg-primary px-4 py-2 text-on-primary',
      attrs: { href: '#main-content' },
    },
    [t('app.skipToContent')]
  );
  root.replaceChildren(skipLink, outlet);

  const router = new Router(outlet, ROUTES, DEFAULT_ROUTE, { onLoading: renderLoading, onError: renderError });

  appEvents.on('locale:changed', ({ locale }) => {
    document.documentElement.lang = locale;
    skipLink.textContent = t('app.skipToContent');
    router.refresh();
  });

  // Web Audio requires a user gesture before sounds can play.
  window.addEventListener('pointerdown', () => audioSynthesizer.unlock(), { once: true });

  router.start();
};
