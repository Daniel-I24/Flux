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
| D-014 | Repositorio | Cada nota se valida por separado al leer | Una nota corrupta no borra las demás. |
| D-015 | Fechas | El parser determinista en español tiene prioridad sobre la fecha que devuelva Gemini | La aritmética de fechas relativas ("en 10 segundos") del LLM no es confiable; la clasificación sí. |
| D-016 | Clasificador local | Reglas con pesos + desempate `task > reminder > idea > info` | "Comprar café el viernes" es una tarea con fecha; un recordatorio necesita "recuérdame/avísame" o solo una fecha. |
| D-017 | Sync | Borrado lógico (`deletedAt`) + last-write-wins por `updatedAt` estrictamente creciente | Permite sincronizar borrados y resolver conflictos sin servidor de estado. |
| D-018 | Cola offline | Outbox persistente por dispositivo/sala; se elimina solo con ack de entrega a un par; se fusionan revisiones de la misma nota | Una nota dictada sin celular conectado llega al reconectar, una sola vez. |
| D-019 | Deduplicación | Ids de mensaje recordados (LRU acotado) + LWW en `NoteService` | Mensajes repetidos por varios transportes o reintentos no duplican notas. |
| D-020 | Transporte | `/demo` usa solo BroadcastChannel; `/watch` y `/phone` usan Socket.IO + BroadcastChannel (compuesto) | La demo funciona sin servidor; los dispositivos reales sincronizan entre equipos y entre pestañas. |
| D-021 | Estado de conexión | "Conectado" solo si el otro dispositivo es alcanzable; si no, "Esperando…" | Un indicador verde sin par al otro lado engañaría en la sustentación. |
| D-022 | Servidor | Sin estado: solo retransmite dentro de la sala; el ack indica si hubo receptor | KISS: el celular es la fuente de verdad de las notas. |
| D-023 | Gemini | REST `generateContent` con salida estructurada (`responseSchema`), key en header, modelo por `GEMINI_MODEL` (por defecto `gemini-2.5-flash`) | Sin SDK extra; la key nunca sale del servidor ni aparece en URLs. |
| D-024 | Fallback | El servidor responde 503 (sin key) o 502 (error de Gemini) y el cliente cae a `LocalNlpStrategy` | El fallback vive en la cadena de estrategias del cliente: funciona incluso sin servidor. |
| D-025 | Esquemas compartidos | Esquemas zod duplicados en cliente y servidor (nota, sobre, clasificación) | Evita un paquete compartido y un paso de build extra; tests cubren ambos lados. |
| D-026 | Recordatorios | Programador por sondeo (1 s) sobre la lista sincronizada; clave `id@dueAt` | Simple, resiste suspensión de pestaña y vuelve a sonar tras posponer. |
| D-027 | Notificaciones | Permiso solo con botón; `registration.showNotification` cuando hay service worker | En Android Chrome `new Notification()` no está permitido. |
| D-028 | PWA | vite-plugin-pwa 0.21 (compatible con Vite 5), SW habilitado también en dev | Permite instalar y probar notificaciones desde `npm run dev` en el emulador. |
| D-029 | Íconos | Generados por `scripts/generate-icons.mjs` (PNG puro con zlib) | Sin dependencias nativas como sharp. |
| D-030 | LAN | `@vitejs/plugin-basic-ssl` + `scripts/dev-lan.mjs` que fija `VITE_PUBLIC_ORIGIN` con la IP local | El micrófono exige HTTPS fuera de localhost y el QR debe apuntar a una URL alcanzable. |
| D-031 | Emulador | `adb reverse` de 5173 y 3001; `-partition-size 2047` por defecto | localhost es contexto seguro en el dispositivo; la partición reducida ayuda con poco disco. |
| D-032 | Niveles de audio | Medidor real (getUserMedia) solo con puntero fino; en móvil, ondas sintéticas | En Android el reconocimiento de voz debe ser el único dueño del micrófono. |
| D-033 | Extensibilidad | Reminders se monta con `mountReminders(host)`; el celular expone `alarmSlot` y `header.addAction` | Agregar el módulo no exigió modificar `PhoneComponent`. |
