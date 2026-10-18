# FLUX — Decisiones técnicas

Cada decisión con una línea de justificación. Se agregan en orden cronológico.

| ID | Tema | Decisión | Justificación |
|---|---|---|---|
| D-001 | Tooling | Vite 5.4 + TS 5.9 strict + Tailwind 3.4 + ESLint 8 (eslintrc) | Versiones ya instaladas y coherentes entre sí; migrar a Tailwind 4 / flat config no aporta a la demo y agrega riesgo. |
| D-002 | Orquestación | `concurrently` en scripts raíz | Arranca frontend y servidor en una sola terminal. |
| D-003 | Código heredado | Se reescribe `src/` sobre la nueva arquitectura; el WIP sin commit se respaldó fuera del repo | El WIP no compilaba, mezclaba responsabilidades y usaba singletons globales incompatibles con dos dispositivos en una misma página. |
| D-004 | Eventos | `EventBus<TEvents>` genérico, una instancia por dispositivo + un bus global de app | Permite simular reloj y celular en la misma pestaña sin compartir eventos internos. |
| D-005 | Errores | Jerarquía `AppError` con `code` que mapea a `errors.<CODE>` en i18n | Un único punto traduce cualquier error técnico a un mensaje claro en español. |
| D-006 | Logging | Fachada `logger` (única con acceso a `console`), silenciosa en tests | Cumple "sin console.log" y mantiene diagnósticos en desarrollo. |
| D-007 | DOM | Helper `h()` que crea nodos y usa texto plano; lint prohíbe `innerHTML` | XSS imposible por construcción al renderizar texto dictado. |
| D-008 | i18n | Diccionarios tipados (`TranslationKey` derivado del español) divididos por área | Una llave mal escrita no compila; archivos pequeños. |
| D-009 | Tokens | Variables CSS en canales RGB + Tailwind `rgb(var(--x) / <alpha>)`; clase `.theme-dark` por subárbol | Un solo lugar define colores claro/oscuro; el reloj puede ser siempre oscuro como Wear OS. |
| D-010 | Router | Router con History API y `import()` por ruta | URLs limpias (`/phone?pair=CODE` para el QR) y code splitting por vista. |
| D-011 | Cambio de idioma | Al cambiar idioma se vuelve a montar la vista activa | KISS: evita suscribir cada componente a `locale:changed`. |
| D-012 | Fuentes | `@fontsource-variable/inter` empaquetada | Funciona sin conexión (PWA) y no depende de Google Fonts. |
| D-013 | Storage | `KeyValueStore` (BrowserStorage/MemoryStorage) + `readValidatedJson` con zod | Ningún servicio toca `localStorage` directamente; datos corruptos se descartan sin romper la app. |
