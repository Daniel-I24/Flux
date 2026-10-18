// Generates the PWA PNG icons without external dependencies (run: node scripts/generate-icons.mjs).
import { mkdirSync, writeFileSync } from 'node:fs';
import { deflateSync } from 'node:zlib';

const OUT_DIR = 'public/icons';
const FROM = [99, 102, 241]; // indigo-500
const TO = [6, 182, 212]; // cyan-500
const WAVE = [[14, 34], [21, 34], [26, 22], [34, 44], [40, 28], [44, 34], [50, 34]]; // 64x64 design units
const SAMPLES = 4;

const crcTable = Array.from({ length: 256 }, (_, n) => {
  let c = n;
  for (let k = 0; k < 8; k += 1) c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
  return c >>> 0;
});
const crc32 = (buffer) => {
  let crc = 0xffffffff;
  for (const byte of buffer) crc = crcTable[(crc ^ byte) & 0xff] ^ (crc >>> 8);
  return (crc ^ 0xffffffff) >>> 0;
};
const chunk = (type, data) => {
  const length = Buffer.alloc(4);
  length.writeUInt32BE(data.length);
  const body = Buffer.concat([Buffer.from(type), data]);
  const crc = Buffer.alloc(4);
  crc.writeUInt32BE(crc32(body));
  return Buffer.concat([length, body, crc]);
};

const distanceToSegment = (px, py, [ax, ay], [bx, by]) => {
  const dx = bx - ax;
  const dy = by - ay;
  const t = Math.max(0, Math.min(1, ((px - ax) * dx + (py - ay) * dy) / (dx * dx + dy * dy)));
  return Math.hypot(px - (ax + t * dx), py - (ay + t * dy));
};

const insideRoundedRect = (x, y, size, radius) => {
  const cx = Math.min(Math.max(x, radius), size - radius);
  const cy = Math.min(Math.max(y, radius), size - radius);
  return Math.hypot(x - cx, y - cy) <= radius;
};

/** maskable: full-bleed background and the glyph shrunk into the 80% safe zone. */
const render = (size, { maskable }) => {
  const rows = [];
  const unit = size / 64;
  const glyphScale = maskable ? 0.72 : 1;
  const offset = (64 - 64 * glyphScale) / 2;
  const stroke = 5 * glyphScale;
  for (let y = 0; y < size; y += 1) {
    const row = [0];
    for (let x = 0; x < size; x += 1) {
      let bg = 0;
      let fg = 0;
      for (let sy = 0; sy < SAMPLES; sy += 1) {
        for (let sx = 0; sx < SAMPLES; sx += 1) {
          const px = x + (sx + 0.5) / SAMPLES;
          const py = y + (sy + 0.5) / SAMPLES;
          if (!maskable && !insideRoundedRect(px, py, size, size * 0.25)) continue;
          bg += 1;
          const gx = (px / unit - offset) / glyphScale;
          const gy = (py / unit - offset) / glyphScale;
          const near = WAVE.slice(1).some((point, index) => distanceToSegment(gx, gy, WAVE[index], point) <= stroke / glyphScale / 2);
          if (near) fg += 1;
        }
      }
      const total = SAMPLES * SAMPLES;
      const mix = (x + y) / (2 * size);
      const base = FROM.map((value, index) => value + (TO[index] - value) * mix);
      const fgRatio = bg === 0 ? 0 : fg / bg;
      const color = base.map((value) => Math.round(value + (255 - value) * fgRatio));
      row.push(...color, Math.round((bg / total) * 255));
    }
    rows.push(Buffer.from(row));
  }
  const header = Buffer.alloc(13);
  header.writeUInt32BE(size, 0);
  header.writeUInt32BE(size, 4);
  header.set([8, 6, 0, 0, 0], 8);
  return Buffer.concat([
    Buffer.from([0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a]),
    chunk('IHDR', header),
    chunk('IDAT', deflateSync(Buffer.concat(rows), { level: 9 })),
    chunk('IEND', Buffer.alloc(0)),
  ]);
};

mkdirSync(OUT_DIR, { recursive: true });
const targets = [
  ['icon-192.png', 192, false],
  ['icon-512.png', 512, false],
  ['maskable-512.png', 512, true],
  ['apple-touch-icon.png', 180, true],
];
for (const [name, size, maskable] of targets) {
  writeFileSync(`${OUT_DIR}/${name}`, render(size, { maskable }));
  process.stdout.write(`created ${OUT_DIR}/${name}\n`);
}
