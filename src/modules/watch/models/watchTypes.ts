import type { ErrorCode } from '../../../core/errors/AppError';
import type { CaptureMode } from '../../../core/types/domain';
import type { ClassificationResult } from '../../ai/models/classification';
import type { ISpeechService } from '../../capture/services/SpeechService';

export type WatchStateId = 'idle' | 'listening' | 'processing' | 'preview' | 'saved';

/** Immutable view model rendered by the watch UI. */
export interface WatchSnapshot {
  state: WatchStateId;
  transcript: string;
  interim: string;
  result: ClassificationResult | null;
  secondsLeft: number;
  editing: boolean;
  textEntry: boolean;
  simulated: boolean;
  error: ErrorCode | null;
  delivered: boolean | null;
}

export type WatchCommand =
  | { type: 'primary' }
  | { type: 'openTextEntry' }
  | { type: 'closeTextEntry' }
  | { type: 'submitText'; text: string }
  | { type: 'simulate'; text: string }
  | { type: 'cancel' }
  | { type: 'edit' }
  | { type: 'cancelEdit' }
  | { type: 'resubmit'; text: string }
  | { type: 'confirm' };

export interface SaveOutcome {
  delivered: boolean;
}

/** Everything the states need, injected so the machine is testable without a browser. */
export interface WatchDependencies {
  classify(text: string): Promise<ClassificationResult>;
  save(result: ClassificationResult): Promise<SaveOutcome>;
  speech: ISpeechService;
  captureMode(): CaptureMode;
  vibrate(pattern: number[]): void;
  timing: {
    autoSaveSeconds: number;
    savedScreenMs: number;
    simulatedWordMs: number;
    vibrationPattern: number[];
  };
}

export const INITIAL_SNAPSHOT: WatchSnapshot = {
  state: 'idle',
  transcript: '',
  interim: '',
  result: null,
  secondsLeft: 0,
  editing: false,
  textEntry: false,
  simulated: false,
  error: null,
  delivered: null,
};
