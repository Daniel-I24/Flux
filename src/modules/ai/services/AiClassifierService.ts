import { ClassificationError, ValidationError } from '../../../core/errors/AppError';
import { logger } from '../../../core/logger/logger';
import type { AiEngine } from '../../../core/types/domain';
import { sanitizeUserText } from '../../../shared/utils/sanitizer';
import { ClassificationSchema, MAX_TEXT_LENGTH, type ClassificationResult } from '../models/classification';
import { parseSpanishDate } from './dateParser';
import type { IClassifierStrategy } from './strategies/IClassifierStrategy';

export interface AiStatus {
  remoteAvailable: boolean;
  lastEngine: AiEngine | null;
}

export interface AiClassifierOptions {
  now?: () => Date;
  timeZone?: () => string;
  onStatusChange?: (status: AiStatus) => void;
}

/**
 * Context of the Strategy pattern: tries each engine in priority order and
 * falls back automatically (remote Gemini → local NLP). Dates found by the
 * deterministic Spanish parser override the model's arithmetic.
 */
export class AiClassifierService {
  private status: AiStatus = { remoteAvailable: false, lastEngine: null };

  constructor(
    private readonly strategies: IClassifierStrategy[],
    private readonly options: AiClassifierOptions = {}
  ) {}

  get currentStatus(): AiStatus {
    return this.status;
  }

  async classify(rawText: string): Promise<ClassificationResult> {
    const text = sanitizeUserText(rawText, MAX_TEXT_LENGTH);
    if (!text) throw new ValidationError('Empty note text');
    const now = this.options.now?.() ?? new Date();
    const timeZone = this.options.timeZone?.() ?? Intl.DateTimeFormat().resolvedOptions().timeZone;

    for (const strategy of this.strategies) {
      if (!(await this.safeIsAvailable(strategy))) continue;
      try {
        const raw = await strategy.classify({ text, now, timeZone });
        const classification = ClassificationSchema.parse(raw);
        this.updateStatus({ lastEngine: strategy.engine });
        return { ...classification, dueAt: this.resolveDueAt(text, now, classification.dueAt), engine: strategy.engine };
      } catch (error) {
        logger.warn(`Classifier "${strategy.engine}" failed, trying next strategy`, error);
        if (strategy.engine === 'remote') this.updateStatus({ remoteAvailable: false });
      }
    }
    throw new ClassificationError('No classifier strategy could handle the note');
  }

  /** Re-checks remote availability (used by the AI status indicator). */
  async refreshStatus(): Promise<AiStatus> {
    const remote = this.strategies.find((strategy) => strategy.engine === 'remote');
    const remoteAvailable = remote ? await this.safeIsAvailable(remote) : false;
    this.updateStatus({ remoteAvailable });
    return this.status;
  }

  private resolveDueAt(text: string, now: Date, modelDueAt: string | null): string | null {
    const deterministic = parseSpanishDate(text, now);
    return deterministic ? deterministic.date.toISOString() : modelDueAt;
  }

  private async safeIsAvailable(strategy: IClassifierStrategy): Promise<boolean> {
    try {
      const available = await strategy.isAvailable();
      if (strategy.engine === 'remote') this.updateStatus({ remoteAvailable: available });
      return available;
    } catch {
      return false;
    }
  }

  private updateStatus(patch: Partial<AiStatus>): void {
    const next = { ...this.status, ...patch };
    if (next.remoteAvailable === this.status.remoteAvailable && next.lastEngine === this.status.lastEngine) return;
    this.status = next;
    this.options.onStatusChange?.(next);
  }
}
