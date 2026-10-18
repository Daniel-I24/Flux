import { describe, expect, it } from 'vitest';
import { LocalNlpStrategy } from '../services/strategies/LocalNlpStrategy';

const NOW = new Date(2026, 9, 18, 10, 0, 0);
const strategy = new LocalNlpStrategy();
const classify = (text: string) => strategy.classify({ text, now: NOW, timeZone: 'America/Bogota' });

describe('LocalNlpStrategy', () => {
  it('is always available', async () => {
    await expect(strategy.isAvailable()).resolves.toBe(true);
  });

  it('classifies reminders with a due date and a clean title', async () => {
    const result = await classify('Recuérdame entregar el proyecto de bases de datos mañana a las 8 am');
    expect(result.category).toBe('reminder');
    expect(result.title).toBe('Entregar el proyecto de bases de datos');
    expect(result.dueAt).toBe(new Date(2026, 9, 19, 8).toISOString());
  });

  it('classifies tasks', async () => {
    const result = await classify('Tengo que comprar los cables HDMI para el laboratorio de redes');
    expect(result.category).toBe('task');
    expect(result.title).toBe('Comprar los cables HDMI para el laboratorio de redes');
    expect(result.dueAt).toBeNull();
  });

  it('treats an action with a date as a task and cleans the title', async () => {
    const result = await classify('Comprar café para la reunión del viernes');
    expect(result.category).toBe('task');
    expect(result.title).toBe('Comprar café para la reunión');
    expect(result.dueAt).not.toBeNull();
  });

  it('classifies a bare date as a reminder', async () => {
    const result = await classify('Reunión con el equipo mañana a las 9');
    expect(result.category).toBe('reminder');
  });

  it('classifies ideas', async () => {
    const result = await classify('Se me ocurrió una idea: usar algoritmos genéticos para ahorrar batería');
    expect(result.category).toBe('idea');
    expect(result.title).toBe('Usar algoritmos genéticos para ahorrar batería');
  });

  it('classifies information', async () => {
    const result = await classify('El código del laboratorio de cómputo es 4829');
    expect(result.category).toBe('info');
  });

  it('falls back to info with low confidence for unknown text', async () => {
    const result = await classify('Hola');
    expect(result.category).toBe('info');
    expect(result.confidence).toBe(0.5);
  });

  it('detects fast reminders in seconds', async () => {
    const result = await classify('Recuérdame tomar agua en 10 segundos');
    expect(result.category).toBe('reminder');
    expect(result.title).toBe('Tomar agua');
    expect(result.dueAt).toBe(new Date(NOW.getTime() + 10_000).toISOString());
  });

  it('sanitizes the stored text', async () => {
    const result = await classify('  <b>comprar</b>   pan  ');
    expect(result.text).toBe('Comprar pan');
  });
});
