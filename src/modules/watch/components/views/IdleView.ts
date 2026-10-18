import { Keyboard, Mic, X } from 'lucide';
import { createButton } from '../../../../shared/components/Button';
import { h, replaceContent } from '../../../../shared/dom/h';
import { icon } from '../../../../shared/dom/icon';
import { t } from '../../../../shared/i18n';
import { TextCaptureForm } from '../../../capture/components/TextCaptureForm';
import type { WatchSnapshot } from '../../models/watchTypes';
import { VIEW_BASE_CLASS, type WatchView, type WatchViewContext } from './WatchView';

export class IdleView implements WatchView {
  readonly element = h('div', { class: VIEW_BASE_CLASS });
  private renderedKey = '';

  constructor(private readonly context: WatchViewContext) {}

  update(snapshot: WatchSnapshot): void {
    const key = `${snapshot.textEntry}|${snapshot.error}`;
    if (key === this.renderedKey) return;
    this.renderedKey = key;
    const error = snapshot.error
      ? h('p', { class: 'max-w-[220px] text-xs font-medium leading-snug text-danger', attrs: { role: 'alert' } }, [
          t(`errors.${snapshot.error}`),
        ])
      : null;
    if (snapshot.textEntry) this.renderTextEntry(error);
    else this.renderMic(error);
  }

  destroy(): void {
    this.element.remove();
  }

  private renderMic(error: HTMLElement | null): void {
    const { dispatch } = this.context;
    const micButton = h(
      'button',
      {
        class:
          'grid h-28 w-28 place-items-center rounded-full bg-gradient-to-br from-primary to-accent text-on-primary ' +
          'shadow-raised transition-transform duration-150 hover:scale-105 active:scale-95 animate-mic-pulse',
        attrs: { type: 'button', 'aria-label': t('watch.micButton') },
        on: { click: () => dispatch({ type: 'primary' }) },
      },
      [icon(Mic, 44, '', 2.2)]
    );
    replaceContent(this.element, [
      micButton,
      h('p', { class: 'text-sm font-medium text-fg' }, [t('watch.tapToSpeak')]),
      error,
      createButton({
        label: t('watch.typeButton'),
        icon: Keyboard,
        iconOnly: true,
        variant: 'secondary',
        onClick: () => dispatch({ type: 'openTextEntry' }),
      }),
    ]);
  }

  private renderTextEntry(error: HTMLElement | null): void {
    const { dispatch, maxLength } = this.context;
    const form = new TextCaptureForm({
      label: t('watch.typePlaceholder'),
      placeholder: t('watch.typePlaceholder'),
      submitLabel: t('watch.submitText'),
      maxLength,
      variant: 'watch',
      onSubmit: (text) => dispatch({ type: 'submitText', text }),
    });
    replaceContent(this.element, [
      h('p', { class: 'text-sm font-semibold text-fg' }, [t('watch.tapToType')]),
      error,
      h('div', { class: 'w-[236px]' }, [form.element]),
      createButton({
        label: t('actions.close'),
        icon: X,
        iconOnly: true,
        variant: 'ghost',
        onClick: () => dispatch({ type: 'closeTextEntry' }),
      }),
    ]);
    queueMicrotask(() => form.focus());
  }
}
