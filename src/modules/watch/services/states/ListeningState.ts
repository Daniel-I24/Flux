import type { AppError } from '../../../../core/errors/AppError';
import type { SpeechSession } from '../../../capture/services/SpeechService';
import type { WatchCommand } from '../../models/watchTypes';
import { IdleState } from './IdleState';
import { ProcessingState } from './ProcessingState';
import { WatchState } from './WatchState';

const SIMULATION_TAIL_MS = 450;
const TEXT_FALLBACK_ERRORS = new Set(['SPEECH_UNSUPPORTED', 'PERMISSION_DENIED', 'NETWORK']);

/**
 * Captures speech. With `simulatedText` (demo cases) it "types" the phrase word by
 * word so the audience sees the same flow without needing a microphone.
 */
export class ListeningState extends WatchState {
  readonly id = 'listening' as const;
  private session: SpeechSession | null = null;
  private finalText = '';

  constructor(
    machine: ConstructorParameters<typeof WatchState>[0],
    private readonly simulatedText?: string
  ) {
    super(machine);
  }

  override enter(): void {
    if (this.simulatedText !== undefined) this.simulate(this.simulatedText);
    else this.listen();
  }

  override exit(): void {
    this.session?.abort();
    this.session = null;
    super.exit();
  }

  handle(command: WatchCommand): void {
    if (command.type === 'primary') this.finish();
    if (command.type === 'cancel') this.machine.transition(new IdleState(this.machine));
  }

  private listen(): void {
    this.session = this.machine.deps.speech.start({
      onInterim: (text) => this.machine.update({ interim: text }),
      onFinal: (text) => {
        this.finalText = `${this.finalText} ${text}`.trim();
        this.machine.update({ interim: this.finalText });
      },
      onError: (error) => this.fail(error),
      onEnd: () => {
        this.session = null;
        this.process();
      },
    });
  }

  /** User tapped stop: keep what was heard; the recognizer then fires onEnd. */
  private finish(): void {
    if (this.session) this.session.stop();
    else this.process();
  }

  private process(): void {
    if (!this.machine.isCurrent(this)) return;
    const text = (this.finalText || this.machine.snapshot.interim).trim();
    if (!text) {
      this.machine.transition(new IdleState(this.machine));
      return;
    }
    this.machine.transition(new ProcessingState(this.machine, text), { transcript: text });
  }

  private fail(error: AppError): void {
    if (!this.machine.isCurrent(this)) return;
    this.machine.transition(new IdleState(this.machine), {
      error: error.code,
      textEntry: TEXT_FALLBACK_ERRORS.has(error.code),
    });
  }

  private simulate(text: string): void {
    const words = text.split(/\s+/).filter(Boolean);
    let shown = 0;
    this.disposables.interval(() => {
      if (shown >= words.length) return;
      shown += 1;
      this.finalText = words.slice(0, shown).join(' ');
      this.machine.update({ interim: this.finalText });
      if (shown === words.length) this.disposables.timeout(() => this.process(), SIMULATION_TAIL_MS);
    }, this.machine.deps.timing.simulatedWordMs);
  }
}
