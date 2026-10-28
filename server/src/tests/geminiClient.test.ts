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

  it('sends the key in a header, restricts categories and parses structured output', async () => {
    const { client, fetchImpl } = clientWith(new Response(JSON.stringify(geminiAnswer(JSON.stringify(SAMPLE)))));
    await expect(client.classify(validBody)).resolves.toEqual(SAMPLE);
    const [url, init] = fetchImpl.mock.calls[0] as unknown as [string, RequestInit];
    expect(url).toContain('gemini-test:generateContent');
    expect(url).not.toContain('test-key');
    expect((init.headers as Record<string, string>)['x-goog-api-key']).toBe('test-key');
    const body = JSON.parse(init.body as string);
    expect(body.generationConfig.responseSchema.properties.categoryId.enum).toEqual(['shopping', 'tasks', 'gym']);
    expect(body.contents[0].parts[0].text).toContain('Gimnasio');
  });

  it('rejects categories the app did not send and malformed answers', async () => {
    const foreign = JSON.stringify({ ...SAMPLE, categoryId: 'hacked' });
    await expect(clientWith(new Response(JSON.stringify(geminiAnswer(foreign)))).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
    await expect(clientWith(new Response(JSON.stringify(geminiAnswer('not json')))).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
  });

  it('wraps HTTP and network failures', async () => {
    await expect(clientWith(new Response('{}', { status: 429 })).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
    await expect(clientWith(new Error('ECONNRESET')).client.classify(validBody)).rejects.toBeInstanceOf(UpstreamError);
  });

  it("reports Google's error status so a bad key is easy to diagnose", async () => {
    const denied = new Response(JSON.stringify({ error: { code: 401, status: 'UNAUTHENTICATED' } }), { status: 401 });
    await expect(clientWith(denied).client.classify(validBody)).rejects.toThrow('Gemini answered HTTP 401 (UNAUTHENTICATED)');
  });
});
