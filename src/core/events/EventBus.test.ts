import { describe, it, expect, vi, beforeEach } from 'vitest';
import { EventBus } from './EventBus';

describe('EventBus (Observer Pattern)', () => {
  let bus: EventBus;

  beforeEach(() => {
    bus = new EventBus();
  });

  it('should subscribe and receive emitted events', () => {
    const handler = vi.fn();
    bus.on('CONNECTION_CHANGED', handler);

    bus.emit('CONNECTION_CHANGED', { isConnected: true });

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith({ isConnected: true });
  });

  it('should unsubscribe cleanly when invoking the returned teardown function', () => {
    const handler = vi.fn();
    const unsubscribe = bus.on('CONNECTION_CHANGED', handler);

    bus.emit('CONNECTION_CHANGED', { isConnected: true });
    expect(handler).toHaveBeenCalledTimes(1);

    unsubscribe();
    bus.emit('CONNECTION_CHANGED', { isConnected: false });
    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('should isolate multiple different event types', () => {
    const connHandler = vi.fn();
    const themeHandler = vi.fn();

    bus.on('CONNECTION_CHANGED', connHandler);
    bus.on('THEME_CHANGED', themeHandler);

    bus.emit('THEME_CHANGED', { mode: 'dark' });

    expect(connHandler).not.toHaveBeenCalled();
    expect(themeHandler).toHaveBeenCalledWith({ mode: 'dark' });
  });
});
