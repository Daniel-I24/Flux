import { ExternalLink, RefreshCw } from 'lucide';
import QRCode from 'qrcode';
import { logger } from '../../../core/logger/logger';
import { createButton } from '../../../shared/components/Button';
import { h } from '../../../shared/dom/h';
import { icon } from '../../../shared/dom/icon';
import { t } from '../../../shared/i18n';

const QR_SIZE = 184;

export interface PairingPanelOptions {
  code: string;
  url: string;
  onRegenerate(): void;
}

/** Card shown next to the watch: QR with the pairing URL, the code and a regenerate action. */
export const createPairingPanel = (options: PairingPanelOptions): HTMLElement => {
  const canvas = h('canvas', {
    class: 'rounded-xl bg-white p-2',
    attrs: { role: 'img', 'aria-label': `${t('watch.pairTitle')}: ${options.url}` },
  });
  QRCode.toCanvas(canvas, options.url, { width: QR_SIZE, margin: 1, errorCorrectionLevel: 'M' }).catch((error: unknown) =>
    logger.warn('QR generation failed', error)
  );

  return h('section', { class: 'flex w-full max-w-sm flex-col items-center gap-4 rounded-3xl border border-border bg-surface p-6 text-center shadow-card', attrs: { 'aria-labelledby': 'pairing-title' } }, [
    h('h2', { class: 'text-lg font-semibold text-fg', attrs: { id: 'pairing-title' } }, [t('watch.pairTitle')]),
    h('p', { class: 'text-sm text-muted' }, [t('watch.pairHint')]),
    canvas,
    h('div', {}, [
      h('p', { class: 'text-xs font-medium uppercase tracking-wide text-muted' }, [t('watch.pairCodeLabel')]),
      h('p', { class: 'mt-1 font-mono text-3xl font-bold tracking-[0.3em] text-fg', attrs: { 'aria-live': 'polite' } }, [options.code]),
    ]),
    h('a', {
      class: 'inline-flex items-center gap-1.5 break-all text-xs text-accent underline-offset-2 hover:underline',
      attrs: { href: options.url, target: '_blank', rel: 'noopener', title: t('watch.openPhoneLink') },
    }, [icon(ExternalLink, 14), options.url]),
    createButton({ label: t('watch.newCode'), icon: RefreshCw, size: 'sm', onClick: options.onRegenerate }),
  ]);
};
