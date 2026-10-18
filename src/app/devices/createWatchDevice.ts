import { APP_CONFIG } from '../../core/config/appConfig';
import { createNoteFromClassification } from '../../modules/notes/models/noteFactory';
import type { Note } from '../../modules/notes/models/note';
import type { SyncService } from '../../modules/sync/services/SyncService';
import { toConnectionView } from '../../modules/sync/components/connectionView';
import { WatchComponent } from '../../modules/watch/components/WatchComponent';
import { WatchStateMachine } from '../../modules/watch/services/WatchStateMachine';
import type { AiClassifierService } from '../../modules/ai/services/AiClassifierService';
import { i18n } from '../../shared/i18n';
import { captureModeService, createClassifier, speechService } from '../services';

export interface WatchDevice {
  component: WatchComponent;
  machine: WatchStateMachine;
  classifier: AiClassifierService;
  destroy(): void;
}

const vibrate = (pattern: number[]) => {
  if (typeof navigator.vibrate === 'function') navigator.vibrate(pattern);
};

/** Composition root of the watch: classifier + state machine + sync, no note storage. */
export const createWatchDevice = (sync: SyncService<Note>, linkLabel?: string): WatchDevice => {
  const classifier = createClassifier();
  const { watch } = APP_CONFIG;
  const machine = new WatchStateMachine({
    classify: (text) => classifier.classify(text),
    save: async (result) => {
      const envelope = sync.publish(createNoteFromClassification(result, 'watch'));
      await sync.flush();
      return { delivered: !sync.isPending(envelope.id) };
    },
    speech: speechService,
    captureMode: () => captureModeService.current,
    vibrate,
    timing: {
      autoSaveSeconds: watch.autoSaveSeconds,
      savedScreenMs: watch.savedScreenMs,
      simulatedWordMs: watch.simulatedWordMs,
      vibrationPattern: [...watch.vibrationPattern],
    },
  });
  const component = new WatchComponent({
    machine,
    maxLength: watch.maxNoteLength,
    autoSaveSeconds: watch.autoSaveSeconds,
    languageTag: i18n.languageTag,
  });
  const unsubscribe = sync.onState((state) => component.setConnection(toConnectionView(state, linkLabel)));
  sync.start();
  void classifier.refreshStatus();

  return {
    component,
    machine,
    classifier,
    destroy: () => {
      unsubscribe();
      sync.stop();
      machine.destroy();
      component.destroy();
    },
  };
};
