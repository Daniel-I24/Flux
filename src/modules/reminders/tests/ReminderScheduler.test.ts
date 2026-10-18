import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import type { Note } from '../../notes/models/note';
import { AlarmQueue } from '../services/AlarmQueue';
import { ReminderScheduler } from '../services/ReminderScheduler';

const START = new Date('2026-10-18T10:00:00.000Z').getTime();

const note = (id: string, dueOffsetMs: number | null, overrides: Partial<Note> = {}): Note => ({
  id,
  title: id,
  text: id,
  category: 'reminder',
  dueAt: dueOffsetMs === null ? null : new Date(START + dueOffsetMs).toISOString(),
  completed: false,
  source: 'watch',
  engine: 'local',
  createdAt: new Date(START).toISOString(),
  updatedAt: new Date(START).toISOString(),
  deletedAt: null,
  ...overrides,
});

const setup = (notes: Note[]) => {
  const source = { notes, list: () => source.notes };
  const onDue = vi.fn();
  const scheduler = new ReminderScheduler(source, { tickMs: 1000, graceMs: 60_000, onDue, now: () => Date.now() });
  return { source, onDue, scheduler };
};

describe('ReminderScheduler', () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(START);
  });
  afterEach(() => vi.useRealTimers());

  it('fires a reminder once when it becomes due', () => {
    const { onDue, scheduler } = setup([note('water', 10_000)]);
    scheduler.start();
    vi.advanceTimersByTime(9_000);
    expect(onDue).not.toHaveBeenCalled();
    vi.advanceTimersByTime(2_000);
    expect(onDue).toHaveBeenCalledTimes(1);
    vi.advanceTimersByTime(10_000);
    expect(onDue).toHaveBeenCalledTimes(1);
    scheduler.stop();
  });

  it('ignores completed, deleted and undated notes', () => {
    const { onDue, scheduler } = setup([
      note('done', 1_000, { completed: true }),
      note('gone', 1_000, { deletedAt: new Date(START).toISOString() }),
      note('nodate', null),
    ]);
    scheduler.start();
    vi.advanceTimersByTime(5_000);
    expect(onDue).not.toHaveBeenCalled();
  });

  it('fires recently missed reminders but not old ones', () => {
    const { onDue, scheduler } = setup([note('recent', -30_000), note('old', -3_600_000)]);
    scheduler.start();
    expect(onDue).toHaveBeenCalledTimes(1);
    expect(onDue.mock.calls[0]?.[0].id).toBe('recent');
  });

  it('fires again after a snooze changes the due date', () => {
    const { source, onDue, scheduler } = setup([note('water', 1_000)]);
    scheduler.start();
    vi.advanceTimersByTime(1_000);
    source.notes = [note('water', 1_000 + 300_000)];
    vi.advanceTimersByTime(300_000);
    expect(onDue).toHaveBeenCalledTimes(2);
  });

  it('picks up reminders synced from another device', () => {
    const { source, onDue, scheduler } = setup([]);
    scheduler.start();
    source.notes = [note('from-watch', 3_000, { source: 'watch' })];
    vi.advanceTimersByTime(4_000);
    expect(onDue).toHaveBeenCalledWith(expect.objectContaining({ id: 'from-watch' }));
  });
});

describe('AlarmQueue', () => {
  it('drops alarms completed or rescheduled elsewhere', () => {
    const queue = new AlarmQueue();
    const listener = vi.fn();
    queue.subscribe(listener);
    const ringing = note('a', 0);
    queue.push(ringing);
    queue.push(note('b', 0));
    expect(queue.current?.id).toBe('a');

    queue.reconcile([{ ...ringing, completed: true }, note('b', 0)]);
    expect(queue.current?.id).toBe('b');
    queue.dismiss('b');
    expect(listener).toHaveBeenLastCalledWith(null, 0);
  });
});
