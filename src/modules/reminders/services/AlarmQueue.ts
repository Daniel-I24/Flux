import type { Unsubscribe } from '../../../core/types/domain';
import { isActive, type Note } from '../../notes/models/note';

type Listener = (current: Note | null, pending: number) => void;

/** Ringing alarms, oldest first. The banner shows the head of the queue. */
export class AlarmQueue {
  private alarms: Note[] = [];
  private readonly listeners = new Set<Listener>();

  get current(): Note | null {
    return this.alarms[0] ?? null;
  }

  push(note: Note): void {
    this.alarms = [...this.alarms.filter((alarm) => alarm.id !== note.id), note];
    this.notify();
  }

  dismiss(noteId: string): void {
    this.alarms = this.alarms.filter((alarm) => alarm.id !== noteId);
    this.notify();
  }

  /** Drops alarms whose note was completed, deleted or rescheduled (possibly on another device). */
  reconcile(notes: Note[]): void {
    const byId = new Map(notes.map((note) => [note.id, note]));
    const next = this.alarms.filter((alarm) => {
      const latest = byId.get(alarm.id);
      return latest !== undefined && isActive(latest) && !latest.completed && latest.dueAt === alarm.dueAt;
    });
    if (next.length === this.alarms.length) return;
    this.alarms = next;
    this.notify();
  }

  subscribe(listener: Listener): Unsubscribe {
    this.listeners.add(listener);
    listener(this.current, this.alarms.length);
    return () => this.listeners.delete(listener);
  }

  private notify(): void {
    this.listeners.forEach((listener) => listener(this.current, this.alarms.length));
  }
}
