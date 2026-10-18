import { describe, expect, it } from 'vitest';
import type { Note } from '../models/note';
import { countByCategory, filterNotes, sortNotes } from '../services/noteFilters';

const note = (id: string, overrides: Partial<Note>): Note => ({
  id,
  title: id,
  text: id,
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

const NOTES = [
  note('a', { title: 'Comprar café', category: 'task', createdAt: '2026-10-18T09:00:00.000Z' }),
  note('b', { title: 'Idea de batería', category: 'idea', createdAt: '2026-10-18T11:00:00.000Z', completed: true }),
  note('c', { title: 'Código 4829', category: 'info', createdAt: '2026-10-18T10:00:00.000Z' }),
];

describe('noteFilters', () => {
  it('sorts pending notes first, newest first', () => {
    expect(sortNotes(NOTES).map((item) => item.id)).toEqual(['c', 'a', 'b']);
  });

  it('filters by category and accent-insensitive query', () => {
    expect(filterNotes(NOTES, { category: 'idea', query: '' }).map((item) => item.id)).toEqual(['b']);
    expect(filterNotes(NOTES, { category: 'all', query: 'CAFE' }).map((item) => item.id)).toEqual(['a']);
    expect(filterNotes(NOTES, { category: 'task', query: 'bateria' })).toEqual([]);
  });

  it('counts per category respecting the query', () => {
    expect(countByCategory(NOTES, '')).toEqual({ all: 3, task: 1, idea: 1, reminder: 0, info: 1 });
    expect(countByCategory(NOTES, 'codigo')).toMatchObject({ all: 1, info: 1, task: 0 });
  });
});
