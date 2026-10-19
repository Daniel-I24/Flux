import request from 'supertest';
import { describe, expect, it } from 'vitest';
import { createApp } from '../app.js';
import { AiUnavailableError, UpstreamError } from '../errors/AppError.js';
import { FakeClassifier, FakeVerifier, SAMPLE, testConfig, VALID_APP_CHECK, VALID_TOKEN, validBody } from './helpers.js';

const appWith = (
  { classifier = new FakeClassifier(), verifier = new FakeVerifier(), env = {} as Record<string, string> } = {}
) => createApp({ config: testConfig(env), classifier, verifier });

const classify = (app = appWith(), token: string | null = VALID_TOKEN) => {
  const call = request(app).post('/api/classify');
  return token ? call.set('Authorization', `Bearer ${token}`) : call;
};

describe('GET /api/health', () => {
  it('reports what is configured without exposing secrets', async () => {
    const response = await request(appWith()).get('/api/health');
    expect(response.status).toBe(200);
    expect(response.body).toEqual({ status: 'ok', ai: { gemini: true, model: 'fake-model' }, auth: { firebase: true } });
    expect(response.headers['x-powered-by']).toBeUndefined();
    expect(response.headers['content-security-policy']).toBeDefined();
  });
});

describe('POST /api/classify authentication', () => {
  it('rejects requests without a token', async () => {
    const response = await classify(appWith(), null).send(validBody);
    expect(response.status).toBe(401);
    expect(response.body.error.code).toBe('UNAUTHORIZED');
  });

  it('rejects an invalid token', async () => {
    const response = await classify(appWith(), 'forged').send(validBody);
    expect(response.status).toBe(401);
  });

  it('accepts a valid token and returns the validated classification', async () => {
    const classifier = new FakeClassifier();
    const response = await classify(appWith({ classifier })).send(validBody);
    expect(response.status).toBe(200);
    expect(response.body).toEqual(SAMPLE);
    expect(classifier.calls[0]?.categories).toHaveLength(3);
  });

  it('requires App Check when enabled', async () => {
    const app = appWith({ env: { APP_CHECK_REQUIRED: 'true' } });
    expect((await classify(app).send(validBody)).status).toBe(401);
    expect((await classify(app).set('X-Firebase-AppCheck', 'fake').send(validBody)).status).toBe(401);
    expect((await classify(app).set('X-Firebase-AppCheck', VALID_APP_CHECK).send(validBody)).status).toBe(200);
  });

  it('answers 503 when Firebase Admin is not configured', async () => {
    const response = await classify(appWith({ verifier: new FakeVerifier(false) })).send(validBody);
    expect(response.status).toBe(503);
    expect(response.body.error.code).toBe('AUTH_UNAVAILABLE');
  });
});

describe('POST /api/classify validation and failures', () => {
  it('rejects invalid bodies with 400', async () => {
    const response = await classify().send({ ...validBody, categories: [] });
    expect(response.status).toBe(400);
    expect(response.body.error.code).toBe('VALIDATION');
  });

  it('rejects oversized bodies', async () => {
    const response = await classify(appWith({ env: { BODY_LIMIT: '1kb' } })).send({ ...validBody, text: 'a'.repeat(2000) });
    expect(response.status).toBe(413);
  });

  it('answers 503 when Gemini is not configured and 502 on upstream errors', async () => {
    const unavailable = new FakeClassifier(false, () => Promise.reject(new AiUnavailableError()));
    expect((await classify(appWith({ classifier: unavailable })).send(validBody)).status).toBe(503);
    const failing = new FakeClassifier(true, () => Promise.reject(new UpstreamError('Gemini answered HTTP 500')));
    const response = await classify(appWith({ classifier: failing })).send(validBody);
    expect(response.status).toBe(502);
    expect(JSON.stringify(response.body)).not.toContain('stack');
  });

  it('limits requests per user', async () => {
    const app = appWith({ env: { RATE_LIMIT_PER_USER: '2' } });
    await classify(app).send(validBody);
    await classify(app).send(validBody);
    expect((await classify(app).send(validBody)).status).toBe(429);
  });
});

describe('security middleware', () => {
  it('only allows configured browser origins', async () => {
    const allowed = await request(appWith()).get('/api/health').set('Origin', 'https://flux.example');
    expect(allowed.headers['access-control-allow-origin']).toBe('https://flux.example');
    const denied = await request(appWith()).get('/api/health').set('Origin', 'https://evil.example');
    expect(denied.headers['access-control-allow-origin']).toBeUndefined();
  });

  it('returns 404 for unknown routes', async () => {
    expect((await request(appWith()).get('/api/nope')).status).toBe(404);
  });
});
