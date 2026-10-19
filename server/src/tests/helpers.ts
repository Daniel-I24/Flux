import { loadConfig, type ServerConfig } from '../config/env.js';
import type { Classification, ClassifyRequest } from '../schemas/classification.js';
import type { RequestVerifier } from '../services/firebaseVerifier.js';
import type { Classifier } from '../services/geminiClient.js';

export const testConfig = (overrides: Record<string, string> = {}): ServerConfig =>
  loadConfig({ NODE_ENV: 'test', CORS_ORIGINS: 'https://flux.example', RATE_LIMIT_PER_USER: '100', RATE_LIMIT_PER_IP: '200', ...overrides });

export const SAMPLE: Classification = {
  categoryId: 'shopping',
  title: 'Comprar pan',
  dueAt: null,
  priority: 'NORMAL',
  requiresAlarm: false,
  confidence: 0.9,
};

export const validBody: ClassifyRequest = {
  text: 'comprar pan',
  now: '2026-10-18T10:00:00.000-05:00',
  timeZone: 'America/Bogota',
  categories: [{ id: 'shopping' }, { id: 'tasks' }, { id: 'gym', name: 'Gimnasio' }],
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

export const VALID_TOKEN = 'valid-token';
export const VALID_APP_CHECK = 'valid-app-check';

/** Accepts only VALID_TOKEN / VALID_APP_CHECK, like Firebase Admin would. */
export class FakeVerifier implements RequestVerifier {
  constructor(readonly enabled = true) {}

  async verifyIdToken(token: string): Promise<{ uid: string }> {
    if (token !== VALID_TOKEN) throw new Error('invalid token');
    return { uid: 'user-1' };
  }

  async verifyAppCheck(token: string): Promise<void> {
    if (token !== VALID_APP_CHECK) throw new Error('invalid app check');
  }
}
