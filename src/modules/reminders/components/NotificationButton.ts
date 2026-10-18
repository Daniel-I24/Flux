import { Bell, BellOff, BellRing } from 'lucide';
import { createButton } from '../../../shared/components/Button';
import { t } from '../../../shared/i18n';
import type { NotificationManager } from '../services/NotificationManager';

/** Header action: asks for notification permission only when the user taps it. */
export const createNotificationButton = (manager: NotificationManager, onMessage: (text: string) => void): HTMLElement => {
  const slot = document.createElement('span');
  slot.className = 'contents';

  const render = () => {
    const state = manager.permission;
    const label =
      state === 'granted'
        ? t('phone.notificationsOn')
        : state === 'denied'
          ? t('phone.notificationsBlocked')
          : state === 'unsupported'
            ? t('phone.notificationsUnsupported')
            : t('phone.notificationsEnable');
    const button = createButton({
      label,
      icon: state === 'granted' ? BellRing : state === 'default' ? Bell : BellOff,
      iconOnly: true,
      variant: state === 'default' ? 'secondary' : 'ghost',
      pressed: state === 'granted',
      onClick: async () => {
        if (state !== 'default') return onMessage(label);
        await manager.requestPermission();
        render();
        onMessage(slot.querySelector('button')?.getAttribute('aria-label') ?? label);
        slot.querySelector('button')?.focus();
      },
    });
    slot.replaceChildren(button);
  };
  render();
  return slot;
};
