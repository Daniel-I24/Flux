import { describe, it, expect } from 'vitest';
import { initializeApp } from './main';

describe('App Bootstrap', () => {
  it('should initialize DOM element without throwing', () => {
    document.body.innerHTML = '<div id="app"></div>';
    expect(() => initializeApp()).not.toThrow();
    const app = document.getElementById('app');
    expect(app?.textContent).toContain('Flux Professional Initialized');
  });
});
