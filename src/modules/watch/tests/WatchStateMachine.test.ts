import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { ClassificationError, SpeechError } from '../../../core/errors/AppError';
import type { CaptureMode } from '../../../core/types/domain';
import type { ClassificationResult } from '../../ai/models/classification';
import type { ISpeechService, SpeechHandlers } from '../../capture/services/SpeechService';
import type { WatchDependencies } from '../models/watchTypes';
import { WatchStateMachine } from '../services/WatchStateMachine';

const RESULT: ClassificationResult = {
  category: 'task',
  title: 'Comprar pan',
  text: 'Comprar pan',
  dueAt: null,
  confidence: 0.8,
  engine: 'local',
};

class FakeSpeech implements ISpeechService {
  supported = true;
  handlers: SpeechHandlers | null = null;
  readonly stop = vi.fn(() => this.handlers?.onEnd());
  readonly abort = vi.fn();
  isSupported = () => this.supported;
  start = (handlers: SpeechHandlers) => {
    this.handlers = handlers;
    return { stop: this.stop, abort: this.abort };
  };
}

const setup = (overrides: Partial<WatchDependencies> = {}, mode: CaptureMode = 'voice') => {
  const speech = new FakeSpeech();
  const deps: WatchDependencies = {
    classify: vi.fn().mockResolvedValue(RESULT),
    save: vi.fn().mockResolvedValue({ delivered: true }),
    speech,
    captureMode: () => mode,
    vibrate: vi.fn(),
    timing: { autoSaveSeconds: 5, savedScreenMs: 1000, simulatedWordMs: 100, vibrationPattern: [50] },
    ...overrides,
  };
  return { machine: new WatchStateMachine(deps), deps, speech };
};

describe('WatchStateMachine', () => {
  beforeEach(() => vi.useFakeTimers());
  afterEach(() => vi.useRealTimers());

  it('runs the full voice flow: idle → listening → processing → preview', async () => {
    const { machine, speech, deps } = setup();
    machine.dispatch({ type: 'primary' });
    expect(machine.snapshot.state).toBe('listening');

    speech.handlers?.onInterim('comprar');
    expect(machine.snapshot.interim).toBe('comprar');
    speech.handlers?.onFinal('comprar pan');
    speech.handlers?.onEnd();
    expect(machine.snapshot.state).toBe('processing');

    await vi.advanceTimersByTimeAsync(0);
    expect(deps.classify).toHaveBeenCalledWith('comprar pan');
    expect(machine.snapshot.state).toBe('preview');
    expect(machine.snapshot.secondsLeft).toBe(5);
  });

  it('auto-saves after the countdown, vibrates and returns to idle', async () => {
    const { machine, deps } = setup();
    machine.dispatch({ type: 'submitText', text: 'comprar pan' });
    await vi.advanceTimersByTimeAsync(0);

    await vi.advanceTimersByTimeAsync(5000);
    expect(deps.save).toHaveBeenCalledWith(RESULT);
    expect(machine.snapshot.state).toBe('saved');
    expect(machine.snapshot.delivered).toBe(true);
    expect(deps.vibrate).toHaveBeenCalledWith([50]);

    await vi.advanceTimersByTimeAsync(1000);
    expect(machine.snapshot.state).toBe('idle');
    expect(machine.snapshot.result).toBeNull();
  });

  it('cancels the preview without saving', async () => {
    const { machine, deps } = setup();
    machine.dispatch({ type: 'submitText', text: 'comprar pan' });
    await vi.advanceTimersByTimeAsync(0);

    machine.dispatch({ type: 'cancel' });
    await vi.advanceTimersByTimeAsync(10_000);

    expect(machine.snapshot.state).toBe('idle');
    expect(deps.save).not.toHaveBeenCalled();
  });

  it('pauses the countdown while editing and re-classifies the edited text', async () => {
    const { machine, deps } = setup();
    machine.dispatch({ type: 'submitText', text: 'comprar pan' });
    await vi.advanceTimersByTimeAsync(0);

    machine.dispatch({ type: 'edit' });
    await vi.advanceTimersByTimeAsync(10_000);
    expect(deps.save).not.toHaveBeenCalled();
    expect(machine.snapshot.editing).toBe(true);

    machine.dispatch({ type: 'resubmit', text: 'comprar leche' });
    await vi.advanceTimersByTimeAsync(0);
    expect(deps.classify).toHaveBeenLastCalledWith('comprar leche');
    expect(machine.snapshot.state).toBe('preview');
  });

  it('saves immediately on confirm only once', async () => {
    const { machine, deps } = setup();
    machine.dispatch({ type: 'submitText', text: 'comprar pan' });
    await vi.advanceTimersByTimeAsync(0);
    machine.dispatch({ type: 'confirm' });
    machine.dispatch({ type: 'confirm' });
    await vi.advanceTimersByTimeAsync(0);
    expect(deps.save).toHaveBeenCalledTimes(1);
  });

  it('falls back to text entry when speech is not supported', () => {
    const { machine, speech } = setup();
    speech.supported = false;
    machine.dispatch({ type: 'primary' });
    expect(machine.snapshot).toMatchObject({ state: 'idle', textEntry: true, error: 'SPEECH_UNSUPPORTED' });
  });

  it('uses text entry when the microphone is switched off', () => {
    const { machine } = setup({}, 'text');
    machine.dispatch({ type: 'primary' });
    expect(machine.snapshot).toMatchObject({ state: 'idle', textEntry: true, error: null });
  });

  it('returns to idle with a text fallback when permission is denied', () => {
    const { machine, speech } = setup();
    machine.dispatch({ type: 'primary' });
    speech.handlers?.onError(new SpeechError('denied', 'PERMISSION_DENIED'));
    expect(machine.snapshot).toMatchObject({ state: 'idle', error: 'PERMISSION_DENIED', textEntry: true });
  });

  it('returns to idle when the user stops without saying anything', () => {
    const { machine } = setup();
    machine.dispatch({ type: 'primary' });
    machine.dispatch({ type: 'primary' });
    expect(machine.snapshot.state).toBe('idle');
  });

  it('reports classification failures', async () => {
    const { machine } = setup({ classify: vi.fn().mockRejectedValue(new ClassificationError('down')) });
    machine.dispatch({ type: 'submitText', text: 'hola' });
    await vi.advanceTimersByTimeAsync(0);
    expect(machine.snapshot).toMatchObject({ state: 'idle', error: 'CLASSIFICATION' });
  });

  it('ignores a classification that arrives after cancelling', async () => {
    let resolve: (value: ClassificationResult) => void = () => undefined;
    const { machine } = setup({ classify: () => new Promise((done) => (resolve = done)) });
    machine.dispatch({ type: 'submitText', text: 'hola' });
    machine.dispatch({ type: 'cancel' });
    resolve(RESULT);
    await vi.advanceTimersByTimeAsync(0);
    expect(machine.snapshot.state).toBe('idle');
  });

  it('simulates dictation word by word for demo cases', async () => {
    const { machine, deps } = setup();
    machine.simulate('tomar agua ya');
    expect(machine.snapshot.state).toBe('listening');

    await vi.advanceTimersByTimeAsync(100);
    expect(machine.snapshot.interim).toBe('tomar');
    await vi.advanceTimersByTimeAsync(800);

    expect(deps.classify).toHaveBeenCalledWith('tomar agua ya');
    expect(machine.snapshot.state).toBe('preview');
    expect(machine.snapshot.simulated).toBe(true);
  });

  it('releases timers on destroy', async () => {
    const { machine, deps } = setup();
    machine.dispatch({ type: 'submitText', text: 'comprar pan' });
    await vi.advanceTimersByTimeAsync(0);
    machine.destroy();
    await vi.advanceTimersByTimeAsync(10_000);
    expect(deps.save).not.toHaveBeenCalled();
  });
});
