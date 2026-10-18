import { isActive, type Note } from '../../notes/models/note';

export interface ReminderSource {
  list(): Note[];
}

export interface ReminderSchedulerOptions {
  tickMs: number;
  /** Reminders that became due before the app opened are still shown if within this window. */
  graceMs: number;
  onDue(note: Note): void;
  now?: () => number;
}

/**
 * Polls the note list and fires each due reminder exactly once per due date.
 * Snoozing changes `dueAt`, which creates a new key so the reminder fires again.
 * Works for notes created on any device because it reads the synced list.
 */
export class ReminderScheduler {
  private readonly fired = new Set<string>();
  private timer: ReturnType<typeof setInterval> | null = null;
  private startedAt = 0;

  constructor(
    private readonly source: ReminderSource,
    private readonly options: ReminderSchedulerOptions
  ) {}

  start(): void {
    if (this.timer) return;
    this.startedAt = this.now();
    this.check();
    this.timer = setInterval(() => this.check(), this.options.tickMs);
  }

  stop(): void {
    if (this.timer) clearInterval(this.timer);
    this.timer = null;
  }

  check(): void {
    const now = this.now();
    const oldestAllowed = this.startedAt - this.options.graceMs;
    this.source.list().forEach((note) => {
      if (!isActive(note) || note.completed || !note.dueAt) return;
      const due = Date.parse(note.dueAt);
      const key = `${note.id}@${note.dueAt}`;
      if (due > now || due < oldestAllowed || this.fired.has(key)) return;
      this.fired.add(key);
      this.options.onDue(note);
    });
  }

  private now(): number {
    return this.options.now?.() ?? Date.now();
  }
}
