import { z } from 'zod';
import { logger } from '../../../core/logger/logger';
import { readValidatedJson, writeJson, type KeyValueStore } from '../../../shared/storage/KeyValueStore';
import { NoteSchema, type Note } from '../models/note';
import type { INoteRepository } from './INoteRepository';

/** Lenient outer schema: each item is validated on its own so one bad note never drops the rest. */
const StoredNotesSchema = z.array(z.unknown());

export class LocalStorageNoteRepository implements INoteRepository {
  constructor(
    private readonly store: KeyValueStore,
    private readonly storageKey: string
  ) {}

  async getAll(): Promise<Note[]> {
    return this.read();
  }

  async getById(id: string): Promise<Note | null> {
    return this.read().find((note) => note.id === id) ?? null;
  }

  async upsert(note: Note): Promise<Note> {
    const valid = NoteSchema.parse(note);
    const others = this.read().filter((stored) => stored.id !== valid.id);
    writeJson(this.store, this.storageKey, [valid, ...others]);
    return valid;
  }

  async clear(): Promise<void> {
    this.store.remove(this.storageKey);
  }

  private read(): Note[] {
    const items = readValidatedJson(this.store, this.storageKey, StoredNotesSchema) ?? [];
    const notes: Note[] = [];
    items.forEach((item) => {
      const parsed = NoteSchema.safeParse(item);
      if (parsed.success) notes.push(parsed.data);
      else logger.warn('Discarding corrupted note', parsed.error.issues);
    });
    return notes;
  }
}
