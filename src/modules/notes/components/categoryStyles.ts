import { Bell, Info, Lightbulb, ListTodo, type IconNode } from 'lucide';
import type { NoteCategory } from '../../../core/types/domain';
import type { ChipTone } from '../../../shared/components/Chip';

export interface CategoryStyle extends ChipTone {
  icon: IconNode;
  border: string;
}

/** Literal class names per category so Tailwind keeps them in the build. */
export const CATEGORY_STYLES: Record<NoteCategory, CategoryStyle> = {
  task: { icon: ListTodo, text: 'text-task', soft: 'bg-task/15', dot: 'bg-task', border: 'border-task' },
  idea: { icon: Lightbulb, text: 'text-idea', soft: 'bg-idea/15', dot: 'bg-idea', border: 'border-idea' },
  reminder: { icon: Bell, text: 'text-reminder', soft: 'bg-reminder/15', dot: 'bg-reminder', border: 'border-reminder' },
  info: { icon: Info, text: 'text-info', soft: 'bg-info/15', dot: 'bg-info', border: 'border-info' },
};
