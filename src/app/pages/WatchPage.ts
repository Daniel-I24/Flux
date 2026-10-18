import type { ViewFactory } from '../../core/router/Router';
import { createPairingPanel } from '../../modules/sync/components/PairingPanel';
import { PairingService } from '../../modules/sync/services/PairingService';
import { createWatchFrame, WATCH_FRAME_SIZE } from '../../modules/watch/components/WatchFrame';
import { fitToContainer } from '../../shared/components/FitToContainer';
import { ScaledBox } from '../../shared/components/ScaledBox';
import { h } from '../../shared/dom/h';
import { createDeviceSync } from '../devices/createDeviceSync';
import { createWatchDevice } from '../devices/createWatchDevice';
import { createPairedTransport, pairingFor, publicOrigin } from '../devices/transports';
import { createBackLink } from './pageChrome';

/** /watch: only the watch, as large as the screen allows, plus the pairing QR. */
export const createWatchPage: ViewFactory = (outlet, context) => {
  const pairing = pairingFor('watch');
  const code = pairing.getOrCreateCode();
  const watch = createWatchDevice(createDeviceSync('watch', code, createPairedTransport('watch', code)));

  const box = new ScaledBox(createWatchFrame(watch.component.element), WATCH_FRAME_SIZE);
  const stage = h('div', { class: 'flex min-h-[60vh] flex-1 items-center justify-center p-4 lg:min-h-0' }, [box.element]);
  const panel = createPairingPanel({
    code,
    url: PairingService.buildPairingUrl(publicOrigin(), code),
    onRegenerate: () => {
      pairing.regenerate();
      context.navigate('/watch');
    },
  });

  outlet.append(
    h('div', { class: 'theme-dark flex min-h-dvh flex-col bg-bg' }, [
      h('div', { class: 'px-2 pt-2' }, [createBackLink(context.navigate)]),
      h('div', { class: 'flex flex-1 flex-col items-center gap-6 px-4 pb-8 lg:flex-row lg:justify-center lg:gap-12 lg:pb-4' }, [stage, panel]),
    ])
  );
  const stopFitting = fitToContainer(box, stage, 0.6, 1.6);

  return {
    destroy: () => {
      stopFitting();
      watch.destroy();
      outlet.replaceChildren();
    },
  };
};
