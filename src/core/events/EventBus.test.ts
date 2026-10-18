import { describe, expect, it, vi } from 'vitest';
import { EventBus } from './EventBus';

interface TestEvents {
  ping: { value: number };
  other: string;
}

describe('EventBus', () => {
  it('delivers typed payloads to every subscriber', () => {
    const bus = new EventBus<TestEvents>();
    const first = vi.fn();
    const second = vi.fn();
    bus.on('ping', first);
    bus.on('ping', second);

    bus.emit('ping', { value: 7 });

    expect(first).toHaveBeenCalledWith({ value: 7 });
    expect(second).toHaveBeenCalledWith({ value: 7 });
  });

  it('stops delivering after unsubscribe and frees the event slot', () => {
    const bus = new EventBus<TestEvents>();
    const handler = vi.fn();
    const unsubscribe = bus.on('ping', handler);

    unsubscribe();
    bus.emit('ping', { value: 1 });

    expect(handler).not.toHaveBeenCalled();
    expect(bus.listenerCount('ping')).toBe(0);
  });

  it('runs once handlers a single time', () => {
    const bus = new EventBus<TestEvents>();
    const handler = vi.fn();
    bus.once('other', handler);

    bus.emit('other', 'a');
    bus.emit('other', 'b');

    expect(handler).toHaveBeenCalledTimes(1);
    expect(handler).toHaveBeenCalledWith('a');
  });

  it('isolates failing handlers and reports the error', () => {
    const reporter = vi.fn();
    const bus = new EventBus<TestEvents>(reporter);
    const healthy = vi.fn();
    bus.on('ping', () => {
      throw new Error('boom');
    });
    bus.on('ping', healthy);

    bus.emit('ping', { value: 2 });

    expect(healthy).toHaveBeenCalled();
    expect(reporter).toHaveBeenCalledWith('ping', expect.any(Error));
  });

  it('keeps independent instances isolated', () => {
    const watchBus = new EventBus<TestEvents>();
    const phoneBus = new EventBus<TestEvents>();
    const handler = vi.fn();
    phoneBus.on('ping', handler);

    watchBus.emit('ping', { value: 3 });

    expect(handler).not.toHaveBeenCalled();
  });

  it('allows a handler to unsubscribe itself while emitting', () => {
    const bus = new EventBus<TestEvents>();
    const later = vi.fn();
    const unsubscribe = bus.on('ping', () => unsubscribe());
    bus.on('ping', later);

    bus.emit('ping', { value: 4 });

    expect(later).toHaveBeenCalledTimes(1);
    expect(bus.listenerCount('ping')).toBe(1);
  });
});
