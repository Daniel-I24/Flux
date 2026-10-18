import { AlarmClock, Bell, Info, Lightbulb, ListTodo, type IconNode } from 'lucide';
import type { NoteCategory } from '../../../core/types/domain';

export interface DemoCase {
  id: 'reminder' | 'task' | 'idea' | 'info' | 'fastAlarm';
  icon: IconNode;
  expected: NoteCategory;
  /** Spoken phrase (Spanish, the language the classifier understands). */
  phrase: string;
}

/** Five one-click cases covering the four categories plus a reminder that fires in seconds. */
export const DEMO_CASES: DemoCase[] = [
  { id: 'reminder', icon: Bell, expected: 'reminder', phrase: 'Recuérdame entregar el proyecto de bases de datos mañana a las 8 am' },
  { id: 'task', icon: ListTodo, expected: 'task', phrase: 'Tengo que comprar los cables HDMI para el laboratorio de redes' },
  { id: 'idea', icon: Lightbulb, expected: 'idea', phrase: 'Se me ocurrió una idea: usar algoritmos genéticos para ahorrar batería en el reloj' },
  { id: 'info', icon: Info, expected: 'info', phrase: 'El código de acceso al laboratorio de cómputo es 4829' },
  { id: 'fastAlarm', icon: AlarmClock, expected: 'reminder', phrase: 'Recuérdame tomar agua en 10 segundos' },
];
