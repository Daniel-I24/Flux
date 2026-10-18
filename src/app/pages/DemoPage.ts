import { APP_CONFIG } from '../../core/config/appConfig';
import type { ViewFactory } from '../../core/router/Router';
import { createPhoneFrame, PHONE_FRAME_SIZE } from '../../modules/phone/components/PhoneFrame';
import { DemoToolbar } from '../../modules/presentation/components/DemoToolbar';
import { DeviceStage } from '../../modules/presentation/components/DeviceStage';
import { SyncConnector } from '../../modules/presentation/components/SyncConnector';
import { LocalSyncTransport } from '../../modules/sync/services/transports/LocalSyncTransport';
import { createWatchFrame, WATCH_FRAME_SIZE } from '../../modules/watch/components/WatchFrame';
import { ScaledBox } from '../../shared/components/ScaledBox';
import { h } from '../../shared/dom/h';
import { i18n, t } from '../../shared/i18n';
import { channelNameFor, createDeviceSync, deviceIdFor } from '../devices/createDeviceSync';
import { createPhoneDevice } from '../devices/createPhoneDevice';
import { createWatchDevice } from '../devices/createWatchDevice';
import { captureModeService } from '../services';

/** /demo: both devices on one screen, linked through the local transport. */
export const createDemoPage: ViewFactory = (outlet, context) => {
  const room = APP_CONFIG.sync.demoRoomCode;
  const linkLabel = t('connection.localDemo');
  const localTransport = (role: 'watch' | 'phone') => new LocalSyncTransport(channelNameFor(room), deviceIdFor(role));

  const watch = createWatchDevice(createDeviceSync('watch', room, localTransport('watch')), linkLabel);
  const phone = createPhoneDevice(createDeviceSync('phone', room, localTransport('phone')), linkLabel);
  const phoneFrame = createPhoneFrame(phone.component.element, i18n.languageTag, t('phone.deviceLabel'));

  const stage = new DeviceStage(
    new ScaledBox(createWatchFrame(watch.component.element), WATCH_FRAME_SIZE),
    new ScaledBox(phoneFrame.element, PHONE_FRAME_SIZE),
    new SyncConnector()
  );
  const toolbar = new DemoToolbar({
    simulate: (phrase) => watch.machine.simulate(phrase),
    resetNotes: () => phone.notes.clearAll(),
    captureMode: captureModeService,
    remoteAiAvailable: () => watch.classifier.currentStatus.remoteAvailable,
    navigate: context.navigate,
  });

  outlet.append(h('div', { class: 'flex min-h-dvh flex-col' }, [toolbar.element, stage.element]));

  return {
    destroy: () => {
      toolbar.destroy();
      stage.destroy();
      watch.destroy();
      phone.destroy();
      phoneFrame.destroy();
      outlet.replaceChildren();
    },
  };
};
