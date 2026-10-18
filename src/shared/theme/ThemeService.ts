import type { EventBus } from '../../core/events/EventBus';
import type { AppEvents } from '../../core/events/appEvents';
import type { ThemeMode } from '../../core/types/domain';
import type { KeyValueStore } from '../storage/KeyValueStore';

const DARK_CLASS = 'dark';
const THEME_COLORS: Record<ThemeMode, string> = { light: '#f4f6fb', dark: '#090b11' };

const isThemeMode = (value: string | null): value is ThemeMode =>
  value === 'light' || value === 'dark';

/** Applies the light/dark token set by toggling a class on the root element. */
export class ThemeService {
  private mode: ThemeMode;

  constructor(
    private readonly store: KeyValueStore,
    private readonly bus: EventBus<AppEvents>,
    private readonly storageKey: string,
    private readonly root: HTMLElement = document.documentElement,
    prefersDark: () => boolean = () => window.matchMedia?.('(prefers-color-scheme: dark)').matches ?? true
  ) {
    const saved = store.get(storageKey);
    this.mode = isThemeMode(saved) ? saved : prefersDark() ? 'dark' : 'light';
  }

  get current(): ThemeMode {
    return this.mode;
  }

  apply(): void {
    this.root.classList.toggle(DARK_CLASS, this.mode === 'dark');
    this.root.style.colorScheme = this.mode;
    document.querySelector('meta[name="theme-color"]')?.setAttribute('content', THEME_COLORS[this.mode]);
  }

  setMode(mode: ThemeMode): void {
    this.mode = mode;
    this.store.set(this.storageKey, mode);
    this.apply();
    this.bus.emit('theme:changed', { theme: mode });
  }

  toggle(): void {
    this.setMode(this.mode === 'dark' ? 'light' : 'dark');
  }
}
