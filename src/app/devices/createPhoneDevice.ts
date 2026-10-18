import { APP_CONFIG } from '../../core/config/appConfig';
import type { Note } from '../../modules/notes/models/note';
import { LocalStorageNoteRepository } from '../../modules/notes/services/LocalStorageNoteRepository';
import { NoteService } from '../../modules/notes/services/NoteService';
import { PhoneComponent } from '../../modules/phone/components/PhoneComponent';
import { toConnectionView } from '../../modules/sync/components/connectionView';
import type { SyncService } from '../../modules/sync/services/SyncService';
import { mountReminders } from '../../modules/reminders/mountReminders';
import { i18n } from '../../shared/i18n';
import { audioSynthesizer } from '../../shared/utils/audioSynthesizer';
import { browserStorage } from '../../shared/storage/KeyValueStore';
import { captureModeService, createClassifier, speechService } from '../services';

export interface PhoneDevice {
  component: PhoneComponent;
  notes: NoteService;
  destroy(): void;
}

/**
 * Composition root of the companion: notes are the source of truth here.
 * Local edits are published to peers; remote revisions are merged (LWW).
 */
export const createPhoneDevice = (sync: SyncService<Note>, linkLabel?: string): PhoneDevice => {
  const notes = new NoteService(new LocalStorageNoteRepository(browserStorage, APP_CONFIG.storageKeys.notes));
  const classifier = createClassifier();
  const component = new PhoneComponent({
    notes,
    classify: (text) => classifier.classify(text),
    speech: speechService,
    captureMode: () => captureModeService.current,
    languageTag: i18n.languageTag,
    maxLength: APP_CONFIG.watch.maxNoteLength,
  });

  const subscriptions = [
    notes.on('note:local-change', ({ note }) => sync.publish(note)),
    sync.onPayload((note) => void notes.applyRemote(note)),
    sync.onState((state) => component.setConnection(toConnectionView(state, linkLabel))),
    mountReminders({
      notes,
      host: {
        alarmSlot: component.alarmSlot,
        addHeaderAction: (element) => component.header.addAction(element),
        showMessage: (text) => component.toasts.show(text),
      },
      languageTag: i18n.languageTag,
      tickMs: APP_CONFIG.reminders.tickMs,
      snoozeMinutes: APP_CONFIG.reminders.snoozeMinutes,
      playAlarm: () => audioSynthesizer.playAlarm(),
      vibrate: (pattern) => {
        if (typeof navigator.vibrate === 'function') navigator.vibrate(pattern);
      },
    }),
  ];
  sync.start();

  return {
    component,
    notes,
    destroy: () => {
      subscriptions.forEach((unsubscribe) => unsubscribe());
      sync.stop();
      component.destroy();
    },
  };
};
