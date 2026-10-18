import type { HttpClient } from '../../../../shared/http/HttpClient';
import { ClassificationSchema, type Classification, type ClassifyRequest } from '../../models/classification';
import { API_PATHS, HealthResponseSchema } from '../../models/serverContracts';
import type { IClassifierStrategy } from './IClassifierStrategy';

export interface GeminiStrategyOptions {
  classifyTimeoutMs: number;
  healthTimeoutMs: number;
  /** How long an availability answer is trusted before asking the server again. */
  availabilityTtlMs?: number;
  now?: () => number;
}

const DEFAULT_TTL_MS = 30_000;

/**
 * Remote strategy: the browser never talks to Gemini directly. It calls our
 * server (which owns the API key) and validates the answer with zod.
 */
export class GeminiRemoteStrategy implements IClassifierStrategy {
  readonly engine = 'remote' as const;
  private cachedAvailability: { value: boolean; checkedAt: number } | null = null;

  constructor(
    private readonly http: HttpClient,
    private readonly options: GeminiStrategyOptions
  ) {}

  async isAvailable(): Promise<boolean> {
    const now = this.now();
    const ttl = this.options.availabilityTtlMs ?? DEFAULT_TTL_MS;
    if (this.cachedAvailability && now - this.cachedAvailability.checkedAt < ttl) {
      return this.cachedAvailability.value;
    }
    const value = await this.http
      .getJson(API_PATHS.health, HealthResponseSchema, this.options.healthTimeoutMs)
      .then((health) => health.ai.gemini)
      .catch(() => false);
    this.cachedAvailability = { value, checkedAt: now };
    return value;
  }

  async classify(request: ClassifyRequest): Promise<Classification> {
    try {
      return await this.http.postJson(
        API_PATHS.classify,
        { text: request.text, now: request.now.toISOString(), timeZone: request.timeZone },
        ClassificationSchema,
        this.options.classifyTimeoutMs
      );
    } catch (error) {
      this.cachedAvailability = { value: false, checkedAt: this.now() };
      throw error;
    }
  }

  private now(): number {
    return this.options.now?.() ?? Date.now();
  }
}
