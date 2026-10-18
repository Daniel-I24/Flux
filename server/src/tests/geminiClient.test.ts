import { describe, expect, it, vi } from 'vitest';
import { AiUnavailableError, UpstreamError } from '../errors/AppError.js';
import { GeminiClient } from '../services/geminiClient.js';
import { SAMPLE, validBody } from './helpers.js';

const geminiAnswer = (text: string) => ({ candidates: [{ content: { parts: [{ text }] } }] });

const clientWith = (response: Response | Error) => {
  const fetchImpl = vi.fn(async () => {
    if (response instanceof Error) throw response;
    return response;
  });
  return { client: new GeminiClient({ apiKey: 'test-key', model: 'gemini-test', timeoutMs: 1000, fetchImpl }), fetchImpl };
};

describe('GeminiClient', () => {
  it('is disabled without an API key', async () => {
    const client = new GeminiClient({ apiKey: '', model: 'm', timeoutMs: 1000 });
    expect(client.enabled).toBe(false);
    await expect(client.classify(validBody)).rejects.toBeInstanceOf(AiUnavailableError);
  });

  it('sends the key in a header and parses structured output', async () => {
    const { client, fetchImpl } = clientWith(new Response(JSON.stringify(geminiAnswer(JSON.stringify(SAMPLE)))));
    await expect(client.classify(validBody)).resolves.toEqual(SAMPLE);
    const [url, init] = fetchImpl.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toContain('gemini-test:generateContent');
    expect(url).not.toContain('test-key');
    expect((init.headers as Record<string, string>)['x-goog-api-key']).toBe('test-key');
  });

  it('rejects answers that do not match the schema', async () => {
    const { client } = clientWith(new Response(JSON.stringify(geminiAnswer('{"category":"other"}'))));
    await expect(client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
  });

  it('rejects invalid JSON and HTTP errors', async () => {
    await expect(clientWith(new Response(JSON.stringify(geminiAnswer('not json')))).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
    await expect(clientWith(new Response('{}', { status: 429 })).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
  });

  it('wraps network failures', async () => {
    await expect(clientWith(new Error('ECONNRESET')).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
  });
});
