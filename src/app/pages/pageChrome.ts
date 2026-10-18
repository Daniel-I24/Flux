import { ArrowLeft } from 'lucide';
import { h } from '../../shared/dom/h';
import { icon } from '../../shared/dom/icon';
import { t } from '../../shared/i18n';

/** Small link back to /demo used by the single-device pages. */
export const createBackLink = (navigate: (path: string) => void, className = ''): HTMLAnchorElement =>
  h(
    'a',
    {
      class: `tap-target inline-flex items-center gap-2 rounded-xl px-3 text-sm font-medium text-muted hover:bg-surface-2 hover:text-fg ${className}`,
      attrs: { href: '/demo' },
      on: {
        click: (event) => {
          event.preventDefault();
          navigate('/demo');
        },
      },
    },
    [icon(ArrowLeft, 18), t('app.backToDemo')]
  );
