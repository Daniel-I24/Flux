// Generates the bundled alarm tones (16-bit mono WAV) into the design system's res/raw.
// Run: node scripts/generate-sounds.mjs
import { mkdirSync, writeFileSync } from 'node:fs';

const OUT = 'core/designsystem/src/main/res/raw';
const RATE = 22050;

/** Each tone: list of [frequencyHz, startSeconds, durationSeconds]. */
const SOUNDS = {
  flux_chime: [[880, 0, 0.18], [1318.5, 0.18, 0.35]],
  flux_pulse: [0, 0.25, 0.5, 0.75].map((t) => [988, t, 0.12]),
  flux_bell: [[659.3, 0, 1.2], [1318.5, 0, 0.8], [1975.5, 0, 0.4]],
  flux_soft: [[523.3, 0, 0.5], [659.3, 0.35, 0.5], [784, 0.7, 0.7]],
};

const render = (tones) => {
  const end = Math.max(...tones.map(([, start, duration]) => start + duration)) + 0.05;
  const samples = new Float32Array(Math.ceil(end * RATE));
  for (const [frequency, start, duration] of tones) {
    const from = Math.floor(start * RATE);
    const length = Math.floor(duration * RATE);
    for (let i = 0; i < length; i += 1) {
      const t = i / RATE;
      const attack = Math.min(1, i / (0.01 * RATE));
      const decay = Math.exp((-3 * i) / length);
      samples[from + i] += 0.35 * attack * decay * Math.sin(2 * Math.PI * frequency * t);
    }
  }
  const data = Buffer.alloc(samples.length * 2);
  samples.forEach((value, i) => data.writeInt16LE(Math.round(Math.max(-1, Math.min(1, value)) * 32767), i * 2));
  const header = Buffer.alloc(44);
  header.write('RIFF', 0);
  header.writeUInt32LE(36 + data.length, 4);
  header.write('WAVEfmt ', 8);
  header.writeUInt32LE(16, 16);
  header.writeUInt16LE(1, 20);
  header.writeUInt16LE(1, 22);
  header.writeUInt32LE(RATE, 24);
  header.writeUInt32LE(RATE * 2, 28);
  header.writeUInt16LE(2, 32);
  header.writeUInt16LE(16, 34);
  header.write('data', 36);
  header.writeUInt32LE(data.length, 40);
  return Buffer.concat([header, data]);
};

mkdirSync(OUT, { recursive: true });
for (const [name, tones] of Object.entries(SOUNDS)) {
  writeFileSync(`${OUT}/${name}.wav`, render(tones));
  process.stdout.write(`created ${OUT}/${name}.wav\n`);
}
