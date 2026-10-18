import { describe, expect, it } from 'vitest';
import { ROUTES } from './app/routes';

describe('route registry', () => {
  it('exposes the three device views', () => {
    expect(ROUTES.map((route) => route.path)).toEqual(['/demo', '/watch', '/phone']);
  });
});
