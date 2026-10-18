import type { WatchCommand } from '../../models/watchTypes';
import { ListeningState } from './ListeningState';
import { ProcessingState } from './ProcessingState';
import { WatchState } from './WatchState';

export class IdleState extends WatchState {
  readonly id = 'idle' as const;

  handle(command: WatchCommand): void {
    switch (command.type) {
      case 'primary':
        return this.startCapture();
      case 'openTextEntry':
        return this.machine.update({ textEntry: true, error: null });
      case 'closeTextEntry':
        return this.machine.update({ textEntry: false, error: null });
      case 'submitText':
        return this.machine.transition(new ProcessingState(this.machine, command.text), { transcript: command.text });
      case 'simulate':
        return this.machine.transition(new ListeningState(this.machine, command.text), { simulated: true });
      default:
        return undefined;
    }
  }

  private startCapture(): void {
    const { deps } = this.machine;
    if (deps.captureMode() === 'text') {
      this.machine.update({ textEntry: true, error: null });
      return;
    }
    if (!deps.speech.isSupported()) {
      this.machine.update({ textEntry: true, error: 'SPEECH_UNSUPPORTED' });
      return;
    }
    this.machine.transition(new ListeningState(this.machine));
  }
}
