import { SendHorizontal } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { h } from '../../../shared/dom/h';
import { sanitizeUserText } from '../../../shared/utils/sanitizer';

export interface TextCaptureOptions {
  label: string;
  placeholder: string;
  submitLabel: string;
  maxLength: number;
  initialValue?: string;
  multiline?: boolean;
  /** Visual density: the watch needs a compact, rounded input. */
  variant?: 'watch' | 'phone';
  onSubmit(text: string): void;
}

/** Text fallback for capture (no speech support, no permission, or user preference). */
export class TextCaptureForm {
  readonly element: HTMLFormElement;
  private readonly field: HTMLInputElement | HTMLTextAreaElement;

  constructor(private readonly options: TextCaptureOptions) {
    const isWatch = options.variant === 'watch';
    const fieldClass =
      'w-full rounded-2xl border border-border bg-surface-2 px-4 text-fg placeholder:text-muted ' +
      (isWatch ? 'h-11 text-sm' : 'min-h-[112px] resize-none py-3 text-base');
    const fieldAttrs = {
      'aria-label': options.label,
      placeholder: options.placeholder,
      maxlength: options.maxLength,
      autocomplete: 'off',
      enterkeyhint: 'send',
    };
    this.field = options.multiline
      ? h('textarea', { class: fieldClass, attrs: { ...fieldAttrs, rows: 3 } })
      : h('input', { class: fieldClass, attrs: { ...fieldAttrs, type: 'text' } });
    this.field.value = options.initialValue ?? '';
    if (options.multiline) {
      this.field.addEventListener('keydown', (event) => {
        if (event instanceof KeyboardEvent && event.key === 'Enter' && !event.shiftKey) {
          event.preventDefault();
          this.element.requestSubmit();
        }
      });
    }

    this.element = h(
      'form',
      {
        class: isWatch ? 'flex w-full items-center gap-2' : 'flex w-full flex-col gap-3',
        on: { submit: (event) => this.handleSubmit(event) },
      },
      [
        this.field,
        createButton({
          label: options.submitLabel,
          type: 'submit',
          variant: 'primary',
          icon: SendHorizontal,
          iconOnly: isWatch,
          className: isWatch ? 'shrink-0' : 'w-full',
        }),
      ]
    );
  }

  get value(): string {
    return this.field.value;
  }

  setValue(text: string): void {
    this.field.value = text;
  }

  focus(): void {
    this.field.focus();
  }

  private handleSubmit(event: SubmitEvent): void {
    event.preventDefault();
    const text = sanitizeUserText(this.field.value, this.options.maxLength);
    if (!text) {
      this.field.focus();
      return;
    }
    this.options.onSubmit(text);
  }
}
