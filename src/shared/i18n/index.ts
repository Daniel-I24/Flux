import { APP_CONFIG } from '../../core/config/appConfig';
import { appEvents } from '../../core/events/appEvents';
import { browserStorage } from '../storage/KeyValueStore';
import { I18nService } from './I18nService';
import type { TranslationKey } from './dictionaries';
import type { TranslationParams } from './types';

export type { TranslationKey } from './dictionaries';

export const i18n = new I18nService(
  browserStorage,
  appEvents,
  APP_CONFIG.storageKeys.locale,
  APP_CONFIG.defaultLocale
);

export const t = (key: TranslationKey, params?: TranslationParams): string => i18n.t(key, params);
