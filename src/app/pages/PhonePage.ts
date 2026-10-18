import { Link2 } from 'lucide';
import { APP_CONFIG } from '../../core/config/appConfig';
import type { ViewFactory } from '../../core/router/Router';
import { openPairingSheet } from '../../modules/phone/components/PairingSheet';
import { NullSyncTransport } from '../../modules/sync/services/transports/NullSyncTransport';
import { createButton } from '../../shared/components/Button';
import { h } from '../../shared/dom/h';
import { t } from '../../shared/i18n';
import { createDeviceSync } from '../devices/createDeviceSync';
import { createPhoneDevice } from '../devices/createPhoneDevice';
import { createPairedTransport, pairingFor } from '../devices/transports';

const UNPAIRED_ROOM = 'NONE';

/** /phone: full-screen companion (real phone or emulator). `?pair=CODE` comes from the QR. */
export const createPhonePage: ViewFactory = (outlet, context) => {
  const pairing = pairingFor('phone');
  const fromQr = context.query.get('pair');
  let justPaired = false;
  if (fromQr) {
    try {
      pairing.setCode(fromQr);
      justPaired = true;
    } catch {
      // Invalid code in the URL: keep the previous pairing and let the user type it.
    }
    window.history.replaceState(null, '', '/phone');
  }
  const code = pairing.getCode();
  const transport = code ? createPairedTransport('phone', code) : new NullSyncTransport();
  const phone = createPhoneDevice(createDeviceSync('phone', code ?? UNPAIRED_ROOM, transport));

  phone.component.header.addAction(
    createButton({
      label: t('phone.pairOpen'),
      icon: Link2,
      iconOnly: true,
      variant: code ? 'ghost' : 'primary',
      onClick: () =>
        openPairingSheet(phone.component.element, {
          currentCode: code,
          codeLength: APP_CONFIG.sync.pairCodeLength,
          onPair: (next) => context.navigate(`/phone?pair=${next}`),
          onUnpair: () => {
            pairing.clear();
            context.navigate('/phone');
          },
        }),
    })
  );
  if (justPaired) phone.component.toasts.show(t('phone.toastPaired'));

  outlet.append(
    h('div', { class: 'mx-auto flex h-dvh w-full max-w-[480px] flex-col bg-bg safe-top sm:border-x sm:border-border' }, [
      h('div', { class: 'min-h-0 flex-1' }, [phone.component.element]),
    ])
  );

  return {
    destroy: () => {
      phone.destroy();
      outlet.replaceChildren();
    },
  };
};
