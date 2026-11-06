#!/usr/bin/env node
// Fails when a tracked code file exceeds the limit. Generated files, lockfiles, XML resources,
// documentation and version catalogs are out of scope by the project's rule.
import { execFileSync } from 'node:child_process';
import { readFileSync } from 'node:fs';

const LIMIT = 150;
const CODE = /\.(kt|kts|ts|tsx|js|mjs|cjs|py|ps1|sh|bat)$/;
const GENERATED = /(^|\/)(gradlew|gradlew\.bat|gradle\/wrapper\/.*)$/;

const lineCount = (path) => {
  const text = readFileSync(path, 'utf8');
  const lines = text.split('\n').length;
  return text.endsWith('\n') ? lines - 1 : lines;
};

const files = execFileSync('git', ['ls-files', '--cached', '--others', '--exclude-standard'], { encoding: 'utf8' })
  .split('\n')
  .filter((path) => CODE.test(path) && !GENERATED.test(path));

const counted = files.map((path) => ({ path, lines: lineCount(path) })).sort((a, b) => b.lines - a.lines);
const over = counted.filter((file) => file.lines > LIMIT);

console.log(`${counted.length} code files; longest: ${counted[0].path} (${counted[0].lines} lines)`);
over.forEach((file) => console.error(`OVER ${LIMIT}: ${file.path} (${file.lines})`));
process.exit(over.length === 0 ? 0 : 1);
