import { z } from 'zod';
import { AiUnavailableError, UpstreamError } from '../errors/AppError.js';
import { ClassificationSchema, type Classification, type ClassifyRequest } from '../schemas/classification.js';
import { buildUserContent, RESPONSE_SCHEMA, SYSTEM_INSTRUCTION } from './classificationPrompt.js';

const API_BASE = 'https://generativelanguage.googleapis.com/v1beta/models';

const GeminiResponseSchema = z.object({
  candidates: z
    .array(z.object({ content: z.object({ parts: z.array(z.object({ text: z.string().optional() })) }) }))
    .min(1),
});

export interface GeminiOptions {
  apiKey: string;
  model: string;
  timeoutMs: number;
  fetchImpl?: typeof fetch;
}

export interface Classifier {
  readonly enabled: boolean;
  readonly model: string | null;
  classify(request: ClassifyRequest): Promise<Classification>;
}

/**
 * Calls Gemini's REST API with structured JSON output. The API key only lives
 * here (server env); the answer is validated with zod before being returned.
 */
export class GeminiClient implements Classifier {
  constructor(private readonly options: GeminiOptions) {}

  get enabled(): boolean {
    return this.options.apiKey.length > 0;
  }

  get model(): string | null {
    return this.enabled ? this.options.model : null;
  }

  async classify(request: ClassifyRequest): Promise<Classification> {
    if (!this.enabled) throw new AiUnavailableError();
    const fetchImpl = this.options.fetchImpl ?? fetch;
    let response: Response;
    try {
      response = await fetchImpl(`${API_BASE}/${encodeURIComponent(this.options.model)}:generateContent`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json', 'x-goog-api-key': this.options.apiKey },
        body: JSON.stringify({
          systemInstruction: { parts: [{ text: SYSTEM_INSTRUCTION }] },
          contents: [{ role: 'user', parts: [{ text: buildUserContent(request) }] }],
          generationConfig: { responseMimeType: 'application/json', responseSchema: RESPONSE_SCHEMA, temperature: 0.2 },
        }),
        signal: AbortSignal.timeout(this.options.timeoutMs),
      });
    } catch (error) {
      throw new UpstreamError('Gemini request failed or timed out', { cause: error });
    }
    if (!response.ok) throw new UpstreamError(`Gemini answered HTTP ${response.status}`);
    return this.parse(await response.json().catch(() => null));
  }

  private parse(body: unknown): Classification {
    const envelope = GeminiResponseSchema.safeParse(body);
    const text = envelope.success ? envelope.data.candidates[0]?.content.parts[0]?.text : undefined;
    if (!text) throw new UpstreamError('Gemini returned an empty answer');
    let json: unknown;
    try {
      json = JSON.parse(text);
    } catch (error) {
      throw new UpstreamError('Gemini returned invalid JSON', { cause: error });
    }
    const classification = ClassificationSchema.safeParse(json);
    if (!classification.success) throw new UpstreamError('Gemini answer does not match the schema', { cause: classification.error });
    return classification.data;
  }
}
