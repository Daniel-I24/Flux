import { APP_CONFIG } from '../../core/config/appConfig';
import { eventBus } from '../../core/events/EventBus';

export type ThemeMode = 'light' | 'dark';

/**
 * Manages application color theme across DOM and local storage.
 */
export class ThemeManager {
  private currentTheme: ThemeMode;

  constructor() {
    const saved = localStorage.getItem(APP_CONFIG.storage.themeKey) as ThemeMode | null;
    if (saved === 'light' || saved === 'dark') {
      this.currentTheme = saved;
    } else {
      this.currentTheme = window.matchMedia && window.matchMedia('(prefers-color-scheme: light)').matches ? 'light' : 'dark';
    }
    this.applyTheme();
  }

  public getTheme(): ThemeMode {
    return this.currentTheme;
  }

  public toggle(): ThemeMode {
    this.currentTheme = this.currentTheme === 'dark' ? 'light' : 'dark';
    this.applyTheme();
    return this.currentTheme;
  }

  public setTheme(mode: ThemeMode): void {
    this.currentTheme = mode;
    this.applyTheme();
  }

  private applyTheme(): void {
    const root = document.documentElement;
    if (this.currentTheme === 'dark') {
      root.classList.add('dark');
    } else {
      root.classList.remove('dark');
    }
    localStorage.setItem(APP_CONFIG.storage.themeKey, this.currentTheme);
    eventBus.emit('THEME_CHANGED', { mode: this.currentTheme });
  }
}

export const themeManager = new ThemeManager();
