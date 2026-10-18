import { describe, expect, it, vi } from 'vitest';
import { ClassificationError, ValidationError } from '../../../core/errors/AppError';
import type { Classification } from '../models/classification';
import { AiClassifierService } from '../services/AiClassifierService';
import type { IClassifierStrategy } from '../services/strategies/IClassifierStrategy';
import { LocalNlpStrategy } from '../services/strategies/LocalNlpStrategy';

const NOW = new Date(2026, 9, 18, 10, 0, 0);

const REMOTE_RESULT: Classification = {
  category: 'task',
  title: 'Comprar pan',
  text: 'Comprar pan',
  dueAt: null,
  confidence: 0.9,
};

const fakeRemote = (overrides: Partial<IClassifierStrategy> = {}): IClassifierStrategy => ({
  engine: 'remote',
  isAvailable: vi.fn().mockResolvedValue(true),
  classify: vi.fn().mockResolvedValue(REMOTE_RESULT),
  ...overrides,
});

const createService = (remote: IClassifierStrategy, onStatusChange = vi.fn()) =>
  new AiClassifierService([remote, new LocalNlpStrategy()], { now: () => NOW, timeZone: () => 'UTC', onStatusChange });

describe('AiClassifierService', () => {
  it('uses the remote strategy when it is available', async () => {
    const result = await createService(fakeRemote()).classify('comprar pan');
    expect(result.engine).toBe('remote');
    expect(result.category).toBe('task');
  });

  it('falls back to local NLP when the remote call fails', async () => {
    const onStatusChange = vi.fn();
    const remote = fakeRemote({ classify: vi.fn().mockRejectedValue(new Error('timeout')) });

    const result = await createService(remote, onStatusChange).classify('Se me ocurrió una idea genial');

    expect(result.engine).toBe('local');
    expect(result.category).toBe('idea');
    expect(onStatusChange).toHaveBeenLastCalledWith({ remoteAvailable: false, lastEngine: 'local' });
  });

  it('skips the remote strategy when the server reports it unavailable', async () => {
    const remote = fakeRemote({ isAvailable: vi.fn().mockResolvedValue(false) });
    const result = await createService(remote).classify('comprar pan');
    expect(remote.classify).not.toHaveBeenCalled();
    expect(result.engine).toBe('local');
  });

  it('falls back when the remote answer does not match the schema', async () => {
    const remote = fakeRemote({ classify: vi.fn().mockResolvedValue({ category: 'unknown' }) });
    const result = await createService(remote).classify('comprar pan');
    expect(result.engine).toBe('local');
  });

  it('prefers the deterministic Spanish date over the model date', async () => {
    const remote = fakeRemote({
      classify: vi.fn().mockResolvedValue({ ...REMOTE_RESULT, category: 'reminder', dueAt: '2030-01-01T00:00:00.000Z' }),
    });
    const result = await createService(remote).classify('recuérdame comprar pan en 10 segundos');
    expect(result.dueAt).toBe(new Date(NOW.getTime() + 10_000).toISOString());
  });

  it('rejects empty text', async () => {
    await expect(createService(fakeRemote()).classify('   ')).rejects.toBeInstanceOf(ValidationError);
  });

  it('throws a ClassificationError when every strategy fails', async () => {
    const broken = fakeRemote({ classify: vi.fn().mockRejectedValue(new Error('down')) });
    const service = new AiClassifierService([broken]);
    await expect(service.classify('hola')).rejects.toBeInstanceOf(ClassificationError);
  });

  it('reports remote availability on refresh', async () => {
    const service = createService(fakeRemote());
    await expect(service.refreshStatus()).resolves.toEqual({ remoteAvailable: true, lastEngine: null });
  });
});
