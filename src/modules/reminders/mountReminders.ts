import type { Unsubscribe } from '../../core/types/domain';
import { t } from '../../shared/i18n';
import { MINUTE_MS } from '../../shared/utils/dateUtils';
import type { Note } from '../notes/models/note';
import type { NoteService } from '../notes/services/NoteService';
import { AlarmBanner } from './components/AlarmBanner';
import { createNotificationButton } from './components/NotificationButton';
import { AlarmQueue } from './services/AlarmQueue';
import { NotificationManager } from './services/NotificationManager';
import { ReminderScheduler } from './services/ReminderScheduler';

/** Where the reminders UI plugs in (the phone exposes these without knowing about reminders). */
export interface ReminderHost {
  alarmSlot: HTMLElement;
  addHeaderAction(element: HTMLElement): void;
  showMessage(text: string): void;
}

export interface ReminderOptions {
  notes: NoteService;
  host: ReminderHost;
  languageTag: string;
  tickMs: number;
  snoozeMinutes: number;
  playAlarm(): void;
  vibrate(pattern: number[]): void;
}

const GRACE_MS = 10 * MINUTE_MS;
const ALARM_VIBRATION = [200, 100, 200, 100, 400];

/** Wires scheduler → queue → banner + sound + vibration + system notification. */
export const mountReminders = (options: ReminderOptions): Unsubscribe => {
  const { notes, host } = options;
  const queue = new AlarmQueue();
  const notifications = new NotificationManager();

  const act = async (note: Note, action: () => Promise<unknown>, message: string) => {
    queue.dismiss(note.id);
    try {
      await action();
      host.showMessage(message);
    } catch {
      host.showMessage(t('errors.NOT_FOUND'));
    }
  };

  const banner = new AlarmBanner(options.languageTag, {
    onSnooze: (note) => void act(note, () => notes.snooze(note.id, options.snoozeMinutes), t('phone.toastSnoozed', { minutes: options.snoozeMinutes })),
    onComplete: (note) => void act(note, () => notes.setCompleted(note.id, true), t('phone.toastCompleted')),
  });
  host.alarmSlot.appendChild(banner.element);
  host.addHeaderAction(createNotificationButton(notifications, host.showMessage));

  const scheduler = new ReminderScheduler(notes, {
    tickMs: options.tickMs,
    graceMs: GRACE_MS,
    onDue: (note) => {
      queue.push(note);
      options.playAlarm();
      options.vibrate(ALARM_VIBRATION);
      void notifications.notify({ title: t('notifications.title'), body: note.title, tag: note.id });
    },
  });

  const subscriptions = [
    queue.subscribe((current, pending) => banner.render(current, pending)),
    notes.on('notes:changed', ({ notes: list }) => {
      queue.reconcile(list);
      scheduler.check();
    }),
  ];
  scheduler.start();

  return () => {
    scheduler.stop();
    subscriptions.forEach((unsubscribe) => unsubscribe());
    banner.element.remove();
  };
};
