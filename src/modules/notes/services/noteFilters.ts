import { NOTE_CATEGORIES, type NoteCategory } from '../../../core/types/domain';
import { normalizeForSearch } from '../../../shared/utils/sanitizer';
import type { Note } from '../models/note';

export type CategoryFilter = NoteCategory | 'all';

export interface NoteFilter {
  category: CategoryFilter;
  query: string;
}

export const EMPTY_FILTER: NoteFilter = { category: 'all', query: '' };

/** Pending first, then newest first. */
export const sortNotes = (notes: Note[]): Note[] =>
  [...notes].sort((a, b) => {
    if (a.completed !== b.completed) return a.completed ? 1 : -1;
    return Date.parse(b.createdAt) - Date.parse(a.createdAt);
  });

export const matchesQuery = (note: Note, query: string): boolean => {
  const needle = normalizeForSearch(query.trim());
  if (!needle) return true;
  return normalizeForSearch(`${note.title} ${note.text}`).includes(needle);
};

export const filterNotes = (notes: Note[], filter: NoteFilter): Note[] =>
  notes.filter(
    (note) => (filter.category === 'all' || note.category === filter.category) && matchesQuery(note, filter.query)
  );

/** Counts per category for the chips (respecting the search query). */
export const countByCategory = (notes: Note[], query: string): Record<CategoryFilter, number> => {
  const matching = notes.filter((note) => matchesQuery(note, query));
  const counts = { all: matching.length } as Record<CategoryFilter, number>;
  NOTE_CATEGORIES.forEach((category) => {
    counts[category] = matching.filter((note) => note.category === category).length;
  });
  return counts;
};
