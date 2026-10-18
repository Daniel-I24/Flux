import { describe, expect, it, vi } from 'vitest';
import { EventBus } from '../../core/events/EventBus';
import type { AppEvents } from '../../core/events/appEvents';
import { MemoryStorage } from '../storage/KeyValueStore';
import { ThemeService } from './ThemeService';

const KEY = 'theme';

describe('ThemeService', () => {
  it('falls back to the system preference when nothing is saved', () => {
    const service = new ThemeService(new MemoryStorage(), new EventBus(), KEY, document.createElement('html'), () => false);
    expect(service.current).toBe('light');
  });

  it('toggles the dark class, persists and notifies', () => {
    const store = new MemoryStorage();
    const bus = new EventBus<AppEvents>();
    const root = document.createElement('html');
    const handler = vi.fn();
    bus.on('theme:changed', handler);
    const service = new ThemeService(store, bus, KEY, root, () => false);

    service.toggle();

    expect(root.classList.contains('dark')).toBe(true);
    expect(store.get(KEY)).toBe('dark');
    expect(handler).toHaveBeenCalledWith({ theme: 'dark' });
  });
});
