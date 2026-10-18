# FLUX Technical Decisions Log

| ID | Context | Decision | 1-Line Justification |
|---|---|---|---|
| D-001 | Tooling & Bundling | Vite 5.4 + TypeScript strict + Tailwind v3 + ESLint 8 | Rock-solid stability on Windows with zero flat-config or v4 syntax friction. |
| D-002 | Monorepo Orchestration | `concurrently` for root dev command | Unifies client (Vite) and server (Express) startup in one terminal task. |
| D-003 | Inter-Device Sync | Observer Pattern via typed `EventBus` | Decouples Smartwatch and Companion Phone modules with zero direct dependencies. |
| D-004 | AI Classification | Strategy Pattern (`GeminiRemoteStrategy` vs `LocalNlpStrategy`) | Enables seamless offline fallback and isolates backend endpoint interaction. |
| D-005 | Notes Persistence | Repository Pattern (`INoteRepository` + `LocalStorageNoteRepository`) | Isolates storage and schema validation (zod) from domain services and UI. |
| D-006 | Smartwatch Lifecycle | State Pattern (`IdleState`, `ListeningState`, `ProcessingState`, `PreviewState`, `SavedState`) | Eliminates nested conditional spaghetti and enforces predictable Wear OS transitions. |
| D-007 | Security & Keys | Zero API keys in frontend, Express serverless-style proxy | Eliminates credential leakage in browser source or client bundles. |
| D-008 | UI Design Tokens | CSS variables in `style.css` referenced by Tailwind | Guarantees instant dark/light mode toggle with full semantic color consistency. |
| D-009 | DOM Security | Strict text sanitizer and element-based building (no raw innerHTML) | Prevents cross-site scripting (XSS) when rendering transcribed user speech. |
