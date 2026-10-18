import { Cpu, Languages, Mic, MicOff, Moon, Sparkles, Sun } from 'lucide';
import type { CaptureMode } from '../../../core/types/domain';
import { createButton } from '../../../shared/components/Button';
import { h, replaceContent } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { i18n, t } from '../../../shared/i18n';
import { themeService } from '../../../shared/theme';

export const createMicToggle = (mode: CaptureMode, onToggle: () => void): HTMLButtonElement => {
  const on = mode === 'voice';
  return createButton({
    label: on ? t('demo.micOn') : t('demo.micOff'),
    icon: on ? Mic : MicOff,
    iconOnly: true,
    size: 'sm',
    pressed: on,
    variant: on ? 'primary' : 'secondary',
    onClick: onToggle,
  });
};

export const createThemeToggle = (): HTMLButtonElement => {
  const isDark = themeService.current === 'dark';
  const button = createButton({
    label: isDark ? t('settings.toLight') : t('settings.toDark'),
    icon: isDark ? Sun : Moon,
    iconOnly: true,
    size: 'sm',
    variant: 'ghost',
    onClick: () => {
      themeService.toggle();
      const next = createThemeToggle();
      button.replaceWith(next);
      next.focus();
    },
  });
  return button;
};

export const createLanguageToggle = (): HTMLButtonElement =>
  createButton({
    label: t('settings.languageShort'),
    icon: Languages,
    size: 'sm',
    variant: 'ghost',
    className: 'px-2',
    onClick: () => i18n.toggle(),
  });

/** Pill telling which classifier is active (remote Gemini or local fallback). */
export const renderAiStatus = (target: HTMLElement, remoteAvailable: boolean): void => {
  target.className =
    'inline-flex h-9 items-center gap-1.5 rounded-full border px-3 text-xs font-semibold ' +
    (remoteAvailable ? 'border-accent/40 bg-accent/10 text-accent' : 'border-border bg-surface-2 text-muted');
  target.setAttribute('title', t('ai.statusLabel'));
  replaceContent(target, [
    icon(remoteAvailable ? Sparkles : Cpu, 14),
    h('span', {}, [remoteAvailable ? t('ai.remote') : t('ai.local')]),
  ]);
};
