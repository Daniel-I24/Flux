import { themeManager } from '../theme/themeManager';

export function createThemeToggle(): HTMLButtonElement {
  const button = document.createElement('button');
  button.type = 'button';
  button.className = 'p-2 rounded-xl bg-flux-surface hover:bg-flux-hover text-slate-300 border border-flux-border transition-colors focus:outline-none focus-visible:ring-2 focus-visible:ring-flux-cyan';
  button.setAttribute('aria-label', 'Alternar modo claro u oscuro');

  const updateIcon = (): void => {
    const isDark = themeManager.getTheme() === 'dark';
    button.title = isDark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro';
    button.innerHTML = isDark
      ? '<svg class="w-4 h-4 text-amber-400" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="5"/><path stroke-linecap="round" stroke-linejoin="round" d="M12 1v2m0 18v2M4.22 4.22l1.42 1.42m12.72 12.72 1.42 1.42M1 12h2m18 0h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42"/></svg>'
      : '<svg class="w-4 h-4 text-flux-cyan" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z"/></svg>';
  };

  updateIcon();
  button.addEventListener('click', () => {
    themeManager.toggle();
    updateIcon();
  });

  return button;
}
