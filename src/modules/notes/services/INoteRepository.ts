import type { Note } from '../models/note';

/** Repository contract; async so a remote implementation can replace it later. */
export interface INoteRepository {
  /** Every stored note, including tombstones. */
  getAll(): Promise<Note[]>;
  getById(id: string): Promise<Note | null>;
  upsert(note: Note): Promise<Note>;
  clear(): Promise<void>;
}
