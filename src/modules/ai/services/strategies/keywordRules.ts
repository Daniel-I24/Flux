import type { NoteCategory } from '../../../../core/types/domain';

export interface KeywordRule {
  category: NoteCategory;
  /** Accent-free regular expressions; each match adds `weight` to the category score. */
  patterns: RegExp[];
  weight: number;
}

export const KEYWORD_RULES: KeywordRule[] = [
  {
    category: 'reminder',
    weight: 2,
    patterns: [/\brecuerd(a|ame|ale|anos)\b/, /\brecordar(me)?\b/, /\brecordatorio\b/, /\bavis(a|ame)\b/, /\balarma\b/, /\bno (te )?olvid(es|ar)\b/],
  },
  {
    category: 'task',
    weight: 1,
    patterns: [
      /\b(tengo|tenemos) que\b/,
      /\b(debo|debemos|hay que|necesito)\b/,
      /\b(comprar|entregar|enviar|terminar|llamar|pagar|hacer|revisar|estudiar|preparar|llevar|reservar|agendar|escribir|imprimir|limpiar)\b/,
      /\b(tarea|pendiente|lista de)\b/,
    ],
  },
  {
    category: 'idea',
    weight: 2,
    patterns: [
      /\bidea\b/,
      /\bse me ocurrio\b/,
      /\b(podriamos|podria|seria (bueno|genial|interesante))\b/,
      /\b(que tal si|y si)\b/,
      /\b(concepto|propuesta|innovar|imagina|quizas)\b/,
    ],
  },
  {
    category: 'info',
    weight: 1,
    patterns: [
      /\b(codigo|clave|contrasena|numero|direccion|telefono|correo|dato|placa|cuenta)\b/,
      /\b(queda en|se llama|es el|es la|son las)\b/,
      /\d{3,}/,
    ],
  },
];

/** Adds extra reminder weight when a concrete date was detected. */
export const DATE_REMINDER_WEIGHT = 1.5;

/** Tie-break order when two categories score the same. */
export const CATEGORY_PRIORITY: NoteCategory[] = ['reminder', 'task', 'idea', 'info'];

/** Spoken prefixes removed when building a short title. */
export const SPOKEN_FILLERS: RegExp[] = [
  /^(por favor\s+)?(recuerdame|recuerdame que|avisame|avisame que|anota( que)?|apunta( que)?|guarda( que)?)\s+/,
  /^(tengo|tenemos) que\s+/,
  /^(se me ocurrio( una idea)?|una idea|idea)\s*(:|,)?\s*/,
  /^(que|de)\s+/,
];
