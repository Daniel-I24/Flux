import { logger } from '../../core/logger/logger';

interface Tone {
  frequency: number;
  startOffset: number;
  duration: number;
}

const CHIME: Tone[] = [
  { frequency: 880, startOffset: 0, duration: 0.12 },
  { frequency: 1320, startOffset: 0.12, duration: 0.18 },
];

const ALARM: Tone[] = [0, 0.3, 0.6].flatMap((offset) => [
  { frequency: 988, startOffset: offset, duration: 0.12 },
  { frequency: 1319, startOffset: offset + 0.13, duration: 0.12 },
]);

const PEAK_GAIN = 0.18;

/**
 * Generates short UI sounds with the Web Audio API (no audio files to ship).
 * The context is created lazily and must be unlocked by a user gesture.
 */
export class AudioSynthesizer {
  private context: AudioContext | null = null;

  unlock(): void {
    const context = this.getContext();
    if (context?.state === 'suspended') void context.resume();
  }

  playChime(): void {
    this.play(CHIME);
  }

  playAlarm(): void {
    this.play(ALARM);
  }

  dispose(): void {
    void this.context?.close();
    this.context = null;
  }

  private getContext(): AudioContext | null {
    if (this.context) return this.context;
    if (typeof window === 'undefined' || typeof window.AudioContext !== 'function') return null;
    this.context = new window.AudioContext();
    return this.context;
  }

  private play(tones: Tone[]): void {
    const context = this.getContext();
    if (!context) return;
    try {
      const start = context.currentTime;
      tones.forEach((tone) => this.scheduleTone(context, tone, start));
    } catch (error) {
      logger.warn('Unable to play sound', error);
    }
  }

  private scheduleTone(context: AudioContext, tone: Tone, start: number): void {
    const oscillator = context.createOscillator();
    const gain = context.createGain();
    const begin = start + tone.startOffset;
    const end = begin + tone.duration;
    oscillator.type = 'sine';
    oscillator.frequency.setValueAtTime(tone.frequency, begin);
    gain.gain.setValueAtTime(0, begin);
    gain.gain.linearRampToValueAtTime(PEAK_GAIN, begin + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.0001, end);
    oscillator.connect(gain).connect(context.destination);
    oscillator.start(begin);
    oscillator.stop(end + 0.02);
  }
}

export const audioSynthesizer = new AudioSynthesizer();
