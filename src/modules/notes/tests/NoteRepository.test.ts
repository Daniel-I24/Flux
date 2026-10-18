import { describe, expect, it } from 'vitest';
import { MemoryStorage } from '../../../shared/storage/KeyValueStore';
import type { Note } from '../models/note';
import { LocalStorageNoteRepository } from '../services/LocalStorageNoteRepository';

const KEY = 'notes';

const note = (id: string, overrides: Partial<Note> = {}): Note => ({
  id,
  title: `Nota ${id}`,
  text: `Texto ${id}`,
  category: 'task',
  dueAt: null,
  completed: false,
  source: 'watch',
  engine: 'local',
  createdAt: '2026-10-18T10:00:00.000Z',
  updatedAt: '2026-10-18T10:00:00.000Z',
  deletedAt: null,
  ...overrides,
});

describe('LocalStorageNoteRepository', () => {
  it('upserts and reads notes', async () => {
    const repository = new LocalStorageNoteRepository(new MemoryStorage(), KEY);
    await repository.upsert(note('a'));
    await repository.upsert(note('a', { completed: true }));
    await repository.upsert(note('b'));

    const all = await repository.getAll();
    expect(all.map((stored) => stored.id)).toEqual(['b', 'a']);
    expect((await repository.getById('a'))?.completed).toBe(true);
    expect(await repository.getById('zzz')).toBeNull();
  });

  it('returns an empty list when the stored JSON is corrupted', async () => {
    const store = new MemoryStorage();
    store.set(KEY, '{not json');
    const repository = new LocalStorageNoteRepository(store, KEY);
    await expect(repository.getAll()).resolves.toEqual([]);
  });

  it('drops only the invalid items and keeps valid ones', async () => {
    const store = new MemoryStorage();
    store.set(KEY, JSON.stringify([note('ok'), { id: 'broken', category: 'nope' }, 42]));
    const repository = new LocalStorageNoteRepository(store, KEY);
    const all = await repository.getAll();
    expect(all.map((stored) => stored.id)).toEqual(['ok']);
  });

  it('refuses to persist invalid notes', async () => {
    const repository = new LocalStorageNoteRepository(new MemoryStorage(), KEY);
    await expect(repository.upsert({ ...note('x'), category: 'bad' } as unknown as Note)).rejects.toThrow();
  });

  it('clears all notes', async () => {
    const repository = new LocalStorageNoteRepository(new MemoryStorage(), KEY);
    await repository.upsert(note('a'));
    await repository.clear();
    await expect(repository.getAll()).resolves.toEqual([]);
  });
});
