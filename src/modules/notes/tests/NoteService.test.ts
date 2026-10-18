import { describe, expect, it, vi } from 'vitest';
import { MemoryStorage } from '../../../shared/storage/KeyValueStore';
import type { ClassificationResult } from '../../ai/models/classification';
import { LocalStorageNoteRepository } from '../services/LocalStorageNoteRepository';
import { NoteService } from '../services/NoteService';

const CLASSIFICATION: ClassificationResult = {
  category: 'reminder',
  title: 'Tomar agua',
  text: 'Recuérdame tomar agua',
  dueAt: null,
  confidence: 0.9,
  engine: 'local',
};

const setup = () => {
  let now = new Date('2026-10-18T10:00:00.000Z');
  const service = new NoteService(new LocalStorageNoteRepository(new MemoryStorage(), 'notes'), () => now);
  return { service, advance: (ms: number) => (now = new Date(now.getTime() + ms)) };
};

describe('NoteService', () => {
  it('creates notes and announces local changes', async () => {
    const { service } = setup();
    const localChange = vi.fn();
    service.on('note:local-change', localChange);

    const note = await service.create(CLASSIFICATION, 'phone');

    expect(service.list()).toEqual([note]);
    expect(localChange).toHaveBeenCalledWith({ note });
  });

  it('completes, snoozes and soft-deletes notes', async () => {
    const { service } = setup();
    const note = await service.create(CLASSIFICATION, 'watch');

    expect((await service.setCompleted(note.id, true)).completed).toBe(true);
    const snoozed = await service.snooze(note.id, 5);
    expect(snoozed.completed).toBe(false);
    expect(snoozed.dueAt).toBe('2026-10-18T10:05:00.000Z');

    const removed = await service.remove(note.id);
    expect(removed.deletedAt).not.toBeNull();
    expect(service.list()).toEqual([]);
  });

  it('always stamps newer revisions, even within the same millisecond', async () => {
    const { service } = setup();
    const note = await service.create(CLASSIFICATION, 'watch');
    const first = await service.setCompleted(note.id, true);
    const second = await service.setCompleted(note.id, false);
    expect(Date.parse(second.updatedAt)).toBeGreaterThan(Date.parse(first.updatedAt));
  });

  it('applies remote revisions with last-write-wins and ignores stale or duplicate ones', async () => {
    const { service, advance } = setup();
    const note = await service.create(CLASSIFICATION, 'watch');
    advance(1000);
    const newer = { ...note, completed: true, updatedAt: new Date(Date.parse(note.updatedAt) + 1000).toISOString() };

    expect(await service.applyRemote(newer)).toBe(true);
    expect(await service.applyRemote(newer)).toBe(false);
    expect(await service.applyRemote(note)).toBe(false);
    expect(service.list()[0]?.completed).toBe(true);
  });

  it('does not echo remote revisions back as local changes', async () => {
    const { service } = setup();
    const localChange = vi.fn();
    service.on('note:local-change', localChange);
    const remote = service.buildFromClassification(CLASSIFICATION, 'watch');

    await service.applyRemote(remote);

    expect(localChange).not.toHaveBeenCalled();
    expect(service.list()).toHaveLength(1);
  });

  it('throws NotFound for unknown notes', async () => {
    const { service } = setup();
    await expect(service.setCompleted('missing', true)).rejects.toThrow('not found');
  });
});
