import type { NoteCategory } from '../../../../core/types/domain';
import { capitalize, normalizeForSearch, sanitizeUserText, truncate } from '../../../../shared/utils/sanitizer';
import { MAX_TEXT_LENGTH, MAX_TITLE_LENGTH, type Classification, type ClassifyRequest } from '../../models/classification';
import { parseSpanishDate } from '../dateParser';
import type { IClassifierStrategy } from './IClassifierStrategy';
import { CATEGORY_PRIORITY, DATE_REMINDER_WEIGHT, KEYWORD_RULES, SPOKEN_FILLERS, TRAILING_CONNECTORS } from './keywordRules';

const TITLE_LENGTH = 60;
const BASE_CONFIDENCE = 0.55;
const CONFIDENCE_PER_POINT = 0.1;
const MAX_CONFIDENCE = 0.95;
const FALLBACK_CONFIDENCE = 0.5;

type Scores = Record<NoteCategory, number>;

const scoreCategories = (normalized: string, hasDate: boolean): Scores => {
  const scores: Scores = { task: 0, idea: 0, reminder: hasDate ? DATE_REMINDER_WEIGHT : 0, info: 0 };
  KEYWORD_RULES.forEach((rule) => {
    const hits = rule.patterns.filter((pattern) => pattern.test(normalized)).length;
    scores[rule.category] += hits * rule.weight;
  });
  return scores;
};

const pickCategory = (scores: Scores): { category: NoteCategory; score: number } => {
  const best = CATEGORY_PRIORITY.reduce((winner, category) =>
    scores[category] > scores[winner] ? category : winner
  );
  return scores[best] > 0 ? { category: best, score: scores[best] } : { category: 'info', score: 0 };
};

/** Removes spoken fillers and date phrases from the original text to get a short title. */
const buildTitle = (text: string, dateMatches: string[]): string => {
  const words = text.split(' ');
  const normalizedWords = words.map(normalizeForSearch);
  let normalized = normalizedWords.join(' ');
  let start = 0;
  SPOKEN_FILLERS.forEach((filler) => {
    const found = filler.exec(normalized);
    if (!found) return;
    const removedWords = found[0].trim().split(' ').length;
    start += removedWords;
    normalized = normalizedWords.slice(start).join(' ');
  });
  let title = words.slice(start).join(' ');
  dateMatches.forEach((match) => {
    const index = normalizeForSearch(title).indexOf(match);
    if (index >= 0) title = `${title.slice(0, index)}${title.slice(index + match.length)}`;
  });
  const cleaned = title
    .replace(/\s+/g, ' ')
    .replace(/[\s,.;:]+$/, '')
    .replace(TRAILING_CONNECTORS, '')
    .trim();
  return truncate(capitalize(cleaned || text), Math.min(TITLE_LENGTH, MAX_TITLE_LENGTH));
};

/** Offline, deterministic classifier based on weighted Spanish keyword rules. */
export class LocalNlpStrategy implements IClassifierStrategy {
  readonly engine = 'local' as const;

  async isAvailable(): Promise<boolean> {
    return true;
  }

  async classify(request: ClassifyRequest): Promise<Classification> {
    const text = sanitizeUserText(request.text, MAX_TEXT_LENGTH);
    const normalized = normalizeForSearch(text);
    const parsedDate = parseSpanishDate(text, request.now);
    const { category, score } = pickCategory(scoreCategories(normalized, parsedDate !== null));

    return {
      category,
      title: buildTitle(text, parsedDate?.matches ?? []),
      text: capitalize(text),
      dueAt: parsedDate ? parsedDate.date.toISOString() : null,
      confidence: score > 0 ? Math.min(MAX_CONFIDENCE, BASE_CONFIDENCE + score * CONFIDENCE_PER_POINT) : FALLBACK_CONFIDENCE,
    };
  }
}
