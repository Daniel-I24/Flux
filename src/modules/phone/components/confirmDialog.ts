import { createButton } from '../../../shared/components/Button';
import { openOverlay } from '../../../shared/components/Overlay';
import { h } from '../../../shared/dom/h';
import { t } from '../../../shared/i18n';

export interface ConfirmOptions {
  title: string;
  description: string;
  confirmLabel: string;
  onConfirm(): void;
}

/** Destructive-action confirmation; Cancel gets initial focus. */
export const confirmAction = (host: HTMLElement, options: ConfirmOptions): void => {
  openOverlay(host, {
    title: options.title,
    description: options.description,
    body: (close) => [
      h('div', { class: 'flex justify-end gap-2' }, [
        createButton({ label: t('actions.cancel'), variant: 'ghost', onClick: close }),
        createButton({
          label: options.confirmLabel,
          variant: 'danger',
          onClick: () => {
            close();
            options.onConfirm();
          },
        }),
      ]),
    ],
  });
};
