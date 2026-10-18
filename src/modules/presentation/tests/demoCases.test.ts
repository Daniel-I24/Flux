import { describe, expect, it } from 'vitest';
import { NOTE_CATEGORIES } from '../../../core/types/domain';
import { LocalNlpStrategy } from '../../ai/services/strategies/LocalNlpStrategy';
import { DEMO_CASES } from '../models/demoCases';

const NOW = new Date(2026, 9, 18, 10, 0, 0);
const strategy = new LocalNlpStrategy();

describe('demo cases', () => {
  it('cover the four categories', () => {
    const covered = new Set(DEMO_CASES.map((demoCase) => demoCase.expected));
    expect([...covered].sort()).toEqual([...NOTE_CATEGORIES].sort());
  });

  it.each(DEMO_CASES)('"$id" is classified as $expected by the local engine', async (demoCase) => {
    const result = await strategy.classify({ text: demoCase.phrase, now: NOW, timeZone: 'America/Bogota' });
    expect(result.category).toBe(demoCase.expected);
  });

  it('includes a reminder that fires within seconds', async () => {
    const fast = DEMO_CASES.find((demoCase) => demoCase.id === 'fastAlarm');
    const result = await strategy.classify({ text: fast?.phrase ?? '', now: NOW, timeZone: 'UTC' });
    const delayMs = Date.parse(result.dueAt ?? '') - NOW.getTime();
    expect(delayMs).toBeGreaterThan(0);
    expect(delayMs).toBeLessThanOrEqual(15_000);
  });
});
