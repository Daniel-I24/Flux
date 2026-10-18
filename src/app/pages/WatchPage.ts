import type { ViewFactory } from '../../core/router/Router';
import { h } from '../../shared/dom/h';
import { t } from '../../shared/i18n';

export const createWatchPage: ViewFactory = (outlet) => {
  outlet.append(h('h1', { class: 'p-8 text-2xl font-semibold' }, [t('watch.deviceLabel')]));
  return { destroy: () => outlet.replaceChildren() };
};
