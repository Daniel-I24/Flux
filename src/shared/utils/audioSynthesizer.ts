/**
 * Audio Synthesizer service using Web Audio API.
 * Produces pure harmonic tones and notifications without external audio assets.
 */

export type SoundType = 'tap' | 'alarm' | 'success' | 'cancel';

export class AudioSynthesizer {
  private audioCtx: AudioContext | null = null;

  private getContext(): AudioContext | null {
    if (typeof window === 'undefined') return null;
    if (!this.audioCtx) {
      const AudioCtxClass = window.AudioContext || (window as unknown as { webkitAudioContext: typeof AudioContext }).webkitAudioContext;
      if (AudioCtxClass) {
        this.audioCtx = new AudioCtxClass();
      }
    }
    if (this.audioCtx && this.audioCtx.state === 'suspended') {
      this.audioCtx.resume().catch(() => {});
    }
    return this.audioCtx;
  }

  public play(type: SoundType): void {
    try {
      const ctx = this.getContext();
      if (!ctx) return;
      const now = ctx.currentTime;

      if (type === 'tap') {
        this.synthesizeTone(ctx, 880, 'sine', now, 0.06, 0.05);
      } else if (type === 'cancel') {
        this.synthesizeTone(ctx, 330, 'triangle', now, 0.12, 0.08);
      } else if (type === 'success') {
        [523.25, 659.25, 783.99, 1046.5].forEach((freq, idx) => {
          this.synthesizeTone(ctx, freq, 'sine', now + idx * 0.06, 0.25, 0.08);
        });
      } else if (type === 'alarm') {
        [587.33, 880.0, 1174.66, 880.0].forEach((freq, idx) => {
          this.synthesizeTone(ctx, freq, 'sine', now + idx * 0.14, 0.35, 0.14);
        });
      }
    } catch (err) {
      console.warn('Audio synthesis unavailable:', err);
    }
  }

  private synthesizeTone(ctx: AudioContext, freq: number, type: OscillatorType, start: number, duration: number, peakVol: number): void {
    const osc = ctx.createOscillator();
    const gain = ctx.createGain();
    osc.type = type;
    osc.frequency.setValueAtTime(freq, start);

    gain.gain.setValueAtTime(0.001, start);
    gain.gain.exponentialRampToValueAtTime(peakVol, start + 0.02);
    gain.gain.exponentialRampToValueAtTime(0.001, start + duration);

    osc.connect(gain);
    gain.connect(ctx.destination);

    osc.start(start);
    osc.stop(start + duration + 0.05);
  }
}

export const audioSynthesizer = new AudioSynthesizer();
