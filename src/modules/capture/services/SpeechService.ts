import { AppError, NetworkError, SpeechError } from '../../../core/errors/AppError';

/** Minimal typing of the Web Speech API (not fully covered by lib.dom). */
interface RecognitionAlternative {
  transcript: string;
}
interface RecognitionResult {
  readonly isFinal: boolean;
  readonly length: number;
  [index: number]: RecognitionAlternative;
}
interface RecognitionEvent {
  resultIndex: number;
  results: { length: number; [index: number]: RecognitionResult };
}
interface RecognitionErrorEvent {
  error: string;
}
export interface RecognitionLike {
  lang: string;
  interimResults: boolean;
  continuous: boolean;
  maxAlternatives: number;
  onresult: ((event: RecognitionEvent) => void) | null;
  onerror: ((event: RecognitionErrorEvent) => void) | null;
  onend: (() => void) | null;
  start(): void;
  stop(): void;
  abort(): void;
}
export type RecognitionConstructor = new () => RecognitionLike;

export interface SpeechHandlers {
  onInterim(text: string): void;
  onFinal(text: string): void;
  onError(error: AppError): void;
  onEnd(): void;
}

export interface SpeechSession {
  /** Stops listening and keeps what was heard. */
  stop(): void;
  /** Stops listening and discards results. */
  abort(): void;
}

export interface ISpeechService {
  isSupported(): boolean;
  start(handlers: SpeechHandlers): SpeechSession;
}

const PERMISSION_ERRORS = new Set(['not-allowed', 'service-not-allowed']);
const SILENT_ERRORS = new Set(['no-speech', 'aborted']);

const resolveBrowserRecognition = (): RecognitionConstructor | undefined => {
  if (typeof window === 'undefined') return undefined;
  const speechWindow = window as unknown as Record<string, RecognitionConstructor | undefined>;
  return speechWindow.SpeechRecognition ?? speechWindow.webkitSpeechRecognition;
};

const mapError = (code: string): AppError => {
  if (PERMISSION_ERRORS.has(code)) return new SpeechError(`Microphone permission: ${code}`, 'PERMISSION_DENIED');
  if (code === 'network') return new NetworkError('Speech recognition service unreachable');
  return new SpeechError(`Speech recognition error: ${code}`);
};

/** Web Speech API adapter (es-CO by default). One recognition object per session. */
export class WebSpeechService implements ISpeechService {
  constructor(
    private readonly lang: string,
    private readonly resolveRecognition: () => RecognitionConstructor | undefined = resolveBrowserRecognition
  ) {}

  isSupported(): boolean {
    return this.resolveRecognition() !== undefined;
  }

  start(handlers: SpeechHandlers): SpeechSession {
    const Recognition = this.resolveRecognition();
    if (!Recognition) {
      handlers.onError(new SpeechError('Speech recognition not supported'));
      return { stop: () => undefined, abort: () => undefined };
    }
    const recognition = new Recognition();
    let active = true;
    recognition.lang = this.lang;
    recognition.interimResults = true;
    recognition.continuous = false;
    recognition.maxAlternatives = 1;

    recognition.onresult = (event) => {
      let interim = '';
      for (let index = event.resultIndex; index < event.results.length; index += 1) {
        const result = event.results[index];
        const transcript = result?.[0]?.transcript ?? '';
        if (result?.isFinal) handlers.onFinal(transcript.trim());
        else interim += transcript;
      }
      if (interim) handlers.onInterim(interim.trim());
    };
    recognition.onerror = (event) => {
      if (!SILENT_ERRORS.has(event.error)) handlers.onError(mapError(event.error));
    };
    recognition.onend = () => {
      if (!active) return;
      active = false;
      handlers.onEnd();
    };

    try {
      recognition.start();
    } catch (error) {
      active = false;
      handlers.onError(new SpeechError(`Speech recognition could not start: ${String(error)}`));
    }

    return {
      stop: () => recognition.stop(),
      abort: () => {
        active = false;
        recognition.onresult = null;
        recognition.onerror = null;
        recognition.abort();
      },
    };
  }
}
