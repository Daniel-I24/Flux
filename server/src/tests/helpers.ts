import { loadConfig, type ServerConfig } from '../config/env.js';
import type { Classification, ClassifyRequest } from '../schemas/classification.js';
import type { Classifier } from '../services/geminiClient.js';

export const testConfig = (overrides: Record<string, string> = {}): ServerConfig =>
  loadConfig({ NODE_ENV: 'test', CORS_ORIGINS: 'http://localhost:5173', RATE_LIMIT_MAX: '100', ...overrides });

export const SAMPLE: Classification = {
  category: 'task',
  title: 'Comprar pan',
  text: 'Comprar pan',
  dueAt: null,
  confidence: 0.9,
};

export class FakeClassifier implements Classifier {
  calls: ClassifyRequest[] = [];
  constructor(
    readonly enabled = true,
    private readonly behavior: () => Promise<Classification> = async () => SAMPLE
  ) {}

  get model(): string | null {
    return this.enabled ? 'fake-model' : null;
  }

  classify(request: ClassifyRequest): Promise<Classification> {
    this.calls.push(request);
    return this.behavior();
  }
}

export const validBody = { text: 'comprar pan', now: '2026-10-18T10:00:00.000Z', timeZone: 'America/Bogota' };
