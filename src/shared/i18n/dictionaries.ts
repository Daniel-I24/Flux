import type { Locale } from '../../core/types/domain';
import { esCommon } from './locales/es/common';
import { esDevices } from './locales/es/devices';
import { esDemo } from './locales/es/demo';
import { enCommon } from './locales/en/common';
import { enDevices } from './locales/en/devices';
import { enDemo } from './locales/en/demo';
import type { DeepStrings, LeafPaths } from './types';

const es = { ...esCommon, ...esDevices, ...esDemo };

export type Dictionary = DeepStrings<typeof es>;
export type TranslationKey = LeafPaths<typeof es>;

export const DICTIONARIES: Record<Locale, Dictionary> = {
  es,
  en: { ...enCommon, ...enDevices, ...enDemo },
};

export const BCP47_TAGS: Record<Locale, string> = {
  es: 'es-CO',
  en: 'en-US',
};
