import { Link2, Unlink } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { openOverlay } from '../../../shared/components/Overlay';
import { h } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';
import { isValidPairCode, normalizePairCode } from '../../../shared/utils/id';

export interface PairingSheetOptions {
  currentCode: string | null;
  codeLength: number;
  onPair(code: string): void;
  onUnpair(): void;
}

/** Lets the user type the watch code (alternative to scanning the QR). */
export const openPairingSheet = (host: HTMLElement, options: PairingSheetOptions): void => {
  const error = h('p', { class: 'min-h-[1.25rem] text-sm text-danger', attrs: { 'aria-live': 'polite', id: 'pair-error' } });
  const input = h('input', {
    class: 'h-14 w-full rounded-2xl border border-border bg-surface-2 text-center font-mono text-2xl font-bold uppercase tracking-[0.4em] text-fg',
    attrs: {
      'aria-label': t('phone.pairInputLabel'),
      'aria-describedby': 'pair-error',
      maxlength: options.codeLength + 2,
      autocomplete: 'off',
      autocapitalize: 'characters',
      inputmode: 'text',
      enterkeyhint: 'go',
      placeholder: 'ABC123',
    },
  });

  openOverlay(host, {
    title: t('phone.pairTitle'),
    description: options.currentCode ? t('phone.pairCurrent', { code: options.currentCode }) : t('phone.pairDescription'),
    placement: 'bottom',
    body: (close) => [
      h('form', {
        class: 'flex flex-col gap-3',
        on: {
          submit: (event) => {
            event.preventDefault();
            const code = normalizePairCode(input.value);
            if (!isValidPairCode(code, options.codeLength)) {
              error.textContent = t('phone.pairInvalid');
              input.focus();
              return;
            }
            close();
            options.onPair(code);
          },
        },
      }, [
        input,
        error,
        createButton({ label: t('phone.pairSubmit'), icon: Link2, type: 'submit', variant: 'primary', className: 'w-full' }),
        options.currentCode
          ? createButton({ label: t('phone.unpair'), icon: Unlink, variant: 'ghost', className: 'w-full text-danger', onClick: () => { close(); options.onUnpair(); } })
          : null,
      ]),
    ],
  });
};
