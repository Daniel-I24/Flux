import type { ZodType } from 'zod';
import { AppError, NetworkError, TimeoutError, ValidationError } from '../../core/errors/AppError';

type FetchLike = (input: string, init?: RequestInit) => Promise<Response>;

/** JSON-over-HTTP client with timeouts and zod validation of every response. */
export class HttpClient {
  constructor(
    private readonly baseUrl: string,
    private readonly fetchImpl: FetchLike = (input, init) => globalThis.fetch(input, init)
  ) {}

  getJson<T>(path: string, schema: ZodType<T>, timeoutMs: number): Promise<T> {
    return this.request(path, { method: 'GET' }, schema, timeoutMs);
  }

  postJson<T>(path: string, body: unknown, schema: ZodType<T>, timeoutMs: number): Promise<T> {
    return this.request(
      path,
      { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) },
      schema,
      timeoutMs
    );
  }

  private async request<T>(path: string, init: RequestInit, schema: ZodType<T>, timeoutMs: number): Promise<T> {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), timeoutMs);
    try {
      const response = await this.fetchImpl(`${this.baseUrl}${path}`, { ...init, signal: controller.signal });
      if (!response.ok) throw new NetworkError(`HTTP ${response.status} for ${path}`);
      const parsed = schema.safeParse(await response.json());
      if (!parsed.success) throw new ValidationError(`Unexpected response shape for ${path}`, { cause: parsed.error });
      return parsed.data;
    } catch (error) {
      if (error instanceof AppError) throw error;
      if (controller.signal.aborted) throw new TimeoutError(`Request to ${path} timed out`);
      throw new NetworkError(`Request to ${path} failed`, { cause: error });
    } finally {
      clearTimeout(timer);
    }
  }
}
