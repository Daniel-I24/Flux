import { describe, expect, it, vi } from 'vitest';
import { EventBus } from '../../core/events/EventBus';
import type { AppEvents } from '../../core/events/appEvents';
import { MemoryStorage } from '../storage/KeyValueStore';
import { I18nService } from './I18nService';

const KEY = 'locale';

const createService = (saved?: string) => {
  const store = new MemoryStorage();
  if (saved) store.set(KEY, saved);
  const bus = new EventBus<AppEvents>();
  return { service: new I18nService(store, bus, KEY, 'es'), store, bus };
};

describe('I18nService', () => {
  it('translates nested keys and interpolates params', () => {
    const { service } = createService();
    expect(service.t('watch.saved')).toBe('Guardada');
    expect(service.t('watch.autoSaveIn', { seconds: 3 })).toBe('Guardando en 3 s');
  });

  it('restores a saved locale and ignores invalid values', () => {
    expect(createService('en').service.current).toBe('en');
    expect(createService('fr').service.current).toBe('es');
  });

  it('persists and announces locale changes', () => {
    const { service, store, bus } = createService();
    const handler = vi.fn();
    bus.on('locale:changed', handler);

    service.toggle();

    expect(service.t('actions.save')).toBe('Save');
    expect(service.languageTag).toBe('en-US');
    expect(store.get(KEY)).toBe('en');
    expect(handler).toHaveBeenCalledWith({ locale: 'en' });
  });
});
