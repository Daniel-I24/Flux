import request from 'supertest';
import { describe, expect, it } from 'vitest';
import { createApp } from '../app.js';
import { AiUnavailableError, UpstreamError } from '../errors/AppError.js';
import { FakeClassifier, SAMPLE, testConfig, validBody } from './helpers.js';

const appWith = (classifier = new FakeClassifier(), overrides: Record<string, string> = {}) =>
  createApp({ config: testConfig(overrides), classifier });

describe('GET /api/health', () => {
  it('reports status and AI availability without exposing secrets', async () => {
    const response = await request(appWith()).get('/api/health');
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ status: 'ok', ai: { gemini: true, model: 'fake-model' } });
    expect(response.headers['x-powered-by']).toBeUndefined();
    expect(response.headers['content-security-policy']).toBeDefined();
  });
});

describe('POST /api/classify', () => {
  it('returns the classification for a valid request', async () => {
    const classifier = new FakeClassifier();
    const response = await request(appWith(classifier)).post('/api/classify').send(validBody);
    expect(response.status).toBe(200);
    expect(response.body).toEqual(SAMPLE);
    expect(classifier.calls[0]?.text).toBe('comprar pan');
  });

  it('rejects invalid bodies with 400', async () => {
    const response = await request(appWith()).post('/api/classify').send({ text: '' });
    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('VALIDATION');
  });

  it('rejects malformed JSON and oversized bodies', async () => {
    const malformed = await request(appWith()).post('/api/classify').set('Content-Type', 'application/json').send('{oops');
    expect(malformed.status).toBe(400);
    const huge = await request(appWith(undefined, { BODY_LIMIT: '1kb' }))
      .post('/api/classify')
      .send({ ...validBody, text: 'a'.repeat(2000) });
    expect(huge.status).toBe(413);
  });

  it('answers 503 when Gemini is not configured', async () => {
    const classifier = new FakeClassifier(false, () => Promise.reject(new AiUnavailableError()));
    const response = await request(appWith(classifier)).post('/api/classify').send(validBody);
    expect(response.status).toBe(503);
    expect(response.body.error.code).toBe('AI_UNAVAILABLE');
  });

  it('answers 502 without leaking upstream details', async () => {
    const classifier = new FakeClassifier(true, () => Promise.reject(new UpstreamError('Gemini answered HTTP 500')));
    const response = await request(appWith(classifier)).post('/api/classify').send(validBody);
    expect(response.status).toBe(502);
    expect(JSON.stringify(response.body)).not.toContain('stack');
  });
});

describe('security middleware', () => {
  it('only allows configured CORS origins', async () => {
    const allowed = await request(appWith()).get('/api/health').set('Origin', 'http://localhost:5173');
    expect(allowed.headers['access-control-allow-origin']).toBe('http://localhost:5173');
    const denied = await request(appWith()).get('/api/health').set('Origin', 'https://evil.example');
    expect(denied.headers['access-control-allow-origin']).toBeUndefined();
  });

  it('rate limits /api', async () => {
    const app = appWith(undefined, { RATE_LIMIT_MAX: '2' });
    await request(app).get('/api/health');
    await request(app).get('/api/health');
    const limited = await request(app).get('/api/health');
    expect(limited.status).toBe(429);
  });

  it('returns 404 for unknown routes', async () => {
    const response = await request(appWith()).get('/api/nope');
    expect(response.status).toBe(404);
  });
});
