import { NotFoundError } from '../../../core/errors/AppError';
import { EventBus } from '../../../core/events/EventBus';
import type { DeviceRole, Unsubscribe } from '../../../core/types/domain';
import { addMinutes } from '../../../shared/utils/dateUtils';
import type { ClassificationResult } from '../../ai/models/classification';
import { isActive, isNewerRevision, type Note } from '../models/note';
import { createNoteFromClassification } from '../models/noteFactory';
import { sortNotes } from './noteFilters';
import type { INoteRepository } from './INoteRepository';

export interface NoteEvents {
  /** Active notes changed (any origin). */
  'notes:changed': { notes: Note[] };
  /** A revision produced on this device that peers should receive. */
  'note:local-change': { note: Note };
}

/** Domain service for notes. Views call it; it never knows about sync or the DOM. */
export class NoteService {
  private readonly bus = new EventBus<NoteEvents>();
  private cache = new Map<string, Note>();
  private loaded = false;
  private lastStamp = 0;

  constructor(
    private readonly repository: INoteRepository,
    private readonly now: () => Date = () => new Date()
  ) {}

  on<K extends keyof NoteEvents>(event: K, handler: (payload: NoteEvents[K]) => void): Unsubscribe {
    return this.bus.on(event, handler);
  }

  async load(): Promise<Note[]> {
    const all = await this.repository.getAll();
    this.cache = new Map(all.map((note) => [note.id, note]));
    this.loaded = true;
    return this.list();
  }

  get isLoaded(): boolean {
    return this.loaded;
  }

  list(): Note[] {
    return sortNotes([...this.cache.values()].filter(isActive));
  }

  buildFromClassification(classification: ClassificationResult, source: DeviceRole): Note {
    return createNoteFromClassification(classification, source, this.now());
  }

  async create(classification: ClassificationResult, source: DeviceRole): Promise<Note> {
    return this.commitLocal(this.buildFromClassification(classification, source));
  }

  async setCompleted(id: string, completed: boolean): Promise<Note> {
    const current = this.require(id);
    return this.commitLocal({ ...current, completed, updatedAt: this.stamp(current) });
  }

  async snooze(id: string, minutes: number): Promise<Note> {
    const current = this.require(id);
    const dueAt = addMinutes(this.now(), minutes).toISOString();
    return this.commitLocal({ ...current, dueAt, completed: false, updatedAt: this.stamp(current) });
  }

  async remove(id: string): Promise<Note> {
    const current = this.require(id);
    const timestamp = this.stamp(current);
    return this.commitLocal({ ...current, deletedAt: timestamp, updatedAt: timestamp });
  }

  /** Merges a revision received from a peer (last-write-wins). Returns false if ignored. */
  async applyRemote(note: Note): Promise<boolean> {
    if (!isNewerRevision(note, this.cache.get(note.id))) return false;
    await this.persist(note);
    return true;
  }

  async clearAll(): Promise<void> {
    await this.repository.clear();
    this.cache.clear();
    this.emitChanged();
  }

  private async commitLocal(note: Note): Promise<Note> {
    const saved = await this.persist(note);
    this.bus.emit('note:local-change', { note: saved });
    return saved;
  }

  private async persist(note: Note): Promise<Note> {
    const saved = await this.repository.upsert(note);
    this.cache.set(saved.id, saved);
    this.emitChanged();
    return saved;
  }

  private require(id: string): Note {
    const note = this.cache.get(id);
    if (!note || !isActive(note)) throw new NotFoundError(`Note ${id} not found`);
    return note;
  }

  /** Strictly increasing timestamps (also past the edited revision) so new edits always win LWW. */
  private stamp(current: Note): string {
    const next = Math.max(this.now().getTime(), this.lastStamp + 1, Date.parse(current.updatedAt) + 1);
    this.lastStamp = next;
    return new Date(next).toISOString();
  }

  private emitChanged(): void {
    this.bus.emit('notes:changed', { notes: this.list() });
  }
}
