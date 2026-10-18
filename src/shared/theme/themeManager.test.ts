import { describe, it, expect, beforeEach } from 'vitest';
import { ThemeManager } from './themeManager';

describe('ThemeManager', () => {
  beforeEach(() => {
    localStorage.clear();
    document.documentElement.className = '';
  });

  it('should toggle theme mode and reflect it in DOM class', () => {
    const manager = new ThemeManager();
    manager.setTheme('dark');
    expect(document.documentElement.classList.contains('dark')).toBe(true);

    const newMode = manager.toggle();
    expect(newMode).toBe('light');
    expect(document.documentElement.classList.contains('dark')).toBe(false);
  });
});
