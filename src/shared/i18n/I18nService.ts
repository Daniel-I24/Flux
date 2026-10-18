import type { EventBus } from '../../core/events/EventBus';
import type { AppEvents } from '../../core/events/appEvents';
import type { Locale } from '../../core/types/domain';
import type { KeyValueStore } from '../storage/KeyValueStore';
import { BCP47_TAGS, DICTIONARIES, type TranslationKey } from './dictionaries';
import type { TranslationParams } from './types';

const PLACEHOLDER = /\{(\w+)\}/g;

const isLocale = (value: string | null): value is Locale => value === 'es' || value === 'en';

const lookup = (tree: unknown, path: string): string | null => {
  const value = path.split('.').reduce<unknown>((node, segment) => {
    if (node && typeof node === 'object' && segment in node) {
      return (node as Record<string, unknown>)[segment];
    }
    return undefined;
  }, tree);
  return typeof value === 'string' ? value : null;
};

export class I18nService {
  private locale: Locale;

  constructor(
    private readonly store: KeyValueStore,
    private readonly bus: EventBus<AppEvents>,
    private readonly storageKey: string,
    fallback: Locale
  ) {
    const saved = store.get(storageKey);
    this.locale = isLocale(saved) ? saved : fallback;
  }

  get current(): Locale {
    return this.locale;
  }

  /** BCP-47 tag used by Intl formatters ("es-CO", "en-US"). */
  get languageTag(): string {
    return BCP47_TAGS[this.locale];
  }

  t(key: TranslationKey, params?: TranslationParams): string {
    const template = lookup(DICTIONARIES[this.locale], key) ?? lookup(DICTIONARIES.es, key) ?? key;
    if (!params) return template;
    return template.replace(PLACEHOLDER, (match, name: string) =>
      name in params ? String(params[name]) : match
    );
  }

  setLocale(locale: Locale): void {
    if (locale === this.locale) return;
    this.locale = locale;
    this.store.set(this.storageKey, locale);
    this.bus.emit('locale:changed', { locale });
  }

  toggle(): void {
    this.setLocale(this.locale === 'es' ? 'en' : 'es');
  }
}
