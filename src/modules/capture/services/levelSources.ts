import { logger } from '../../../core/logger/logger';

export type LevelSource = () => number;

const IDLE_LEVEL = 0.18;
const BUMP_LEVEL = 0.9;
const DECAY = 0.9;

/**
 * Plausible voice-like levels for when the microphone is not read directly
 * (simulated demo cases or mobile browsers where speech owns the mic).
 */
export const createSyntheticLevels = (random: () => number = Math.random) => {
  let energy = 0.5;
  return {
    next: (): number => {
      energy = Math.max(IDLE_LEVEL, energy * DECAY);
      return Math.min(1, energy * (0.6 + random() * 0.6));
    },
    bump: (): void => {
      energy = BUMP_LEVEL;
    },
  };
};

/** Real microphone level (RMS) via getUserMedia + AnalyserNode. Must be stopped to free the mic. */
export class AudioLevelMeter {
  private stream: MediaStream | null = null;
  private context: AudioContext | null = null;
  private analyser: AnalyserNode | null = null;
  private buffer: Uint8Array<ArrayBuffer> | null = null;

  async start(): Promise<boolean> {
    try {
      this.stream = await navigator.mediaDevices.getUserMedia({ audio: true });
      this.context = new AudioContext();
      this.analyser = this.context.createAnalyser();
      this.analyser.fftSize = 512;
      this.buffer = new Uint8Array(new ArrayBuffer(this.analyser.fftSize));
      this.context.createMediaStreamSource(this.stream).connect(this.analyser);
      return true;
    } catch (error) {
      logger.warn('Audio level meter unavailable', error);
      this.stop();
      return false;
    }
  }

  readonly level: LevelSource = () => {
    if (!this.analyser || !this.buffer) return 0;
    this.analyser.getByteTimeDomainData(this.buffer);
    let sum = 0;
    this.buffer.forEach((sample) => {
      const centered = (sample - 128) / 128;
      sum += centered * centered;
    });
    return Math.min(1, Math.sqrt(sum / this.buffer.length) * 4);
  };

  stop(): void {
    this.stream?.getTracks().forEach((track) => track.stop());
    void this.context?.close().catch(() => undefined);
    this.stream = null;
    this.context = null;
    this.analyser = null;
    this.buffer = null;
  }
}
