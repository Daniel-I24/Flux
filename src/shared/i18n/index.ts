import { es } from './es';
import { en } from './en';
import { APP_CONFIG } from '../../core/config/appConfig';
import { eventBus } from '../../core/events/EventBus';

type TranslationTree = typeof es;
let currentLocale = (localStorage.getItem(APP_CONFIG.storage.languageKey) || APP_CONFIG.app.defaultLanguage) === 'en' ? 'en' : 'es';

export function getLocale(): string {
  return currentLocale;
}

export function setLocale(locale: 'es' | 'en'): void {
  currentLocale = locale;
  localStorage.setItem(APP_CONFIG.storage.languageKey, locale);
  eventBus.emit('LOCALE_CHANGED', { locale });
}

export function t(path: string): string {
  const dict: TranslationTree = currentLocale === 'en' ? (en as TranslationTree) : es;
  const keys = path.split('.');
  let curr: unknown = dict;
  for (const k of keys) {
    if (curr && typeof curr === 'object' && k in (curr as Record<string, unknown>)) {
      curr = (curr as Record<string, unknown>)[k];
    } else {
      return path;
    }
  }
  return typeof curr === 'string' ? curr : path;
}
