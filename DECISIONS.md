# FLUX nativo — Decisiones técnicas

Cada decisión con una línea de justificación, en orden cronológico. Las decisiones del simulador web están en el tag `web-simulator-v1`.

| ID | Tema | Decisión | Justificación |
|---|---|---|---|
| N-001 | Versión web | Se conserva en el tag `web-simulator-v1` y se elimina de `main` | La app nativa la reemplaza; el tag permite recuperarla. |
| N-002 | Versiones | AGP 8.13.0, Gradle 8.14.3, Kotlin 2.2.20, Compose BOM 2025.08.00, Hilt 2.57.1, Room 2.7.2, Wear Compose 1.5.0, Firebase BoM 34.0.0 | Conjunto estable y compatible entre sí; AGP y Gradle ya estaban en la caché local, importante con poco disco libre. |
| N-003 | JDK | Gradle corre con JDK 21 (bytecode 17) | El JBR de Android Studio instalado es JDK 25, que Gradle 8.14 no soporta. |
| N-004 | Build | Plugins de convención en `build-logic` (`flux.jvm.library`, `flux.android.library`, `flux.android.application`, `flux.android.compose`, `flux.hilt`) | DRY: SDK, Kotlin, Compose, Hilt, firma y calidad se configuran una sola vez. |
| N-005 | Módulos puros | `:core:model`, `:core:domain` y `:core:ai` son módulos Kotlin/JVM | Más rápidos de compilar y probar; no dependen de Android. |
| N-006 | Firebase en el reloj | Los adaptadores de Firebase (Auth, Firestore, App Check) viven en `:mobile`, no en `:core:data` | El reloj no usa cuenta propia; así su APK es más liviano. `:core:data` tiene Room, DataStore y repositorios compartidos. |
| N-007 | Firebase sin `google-services.json` | `FirebaseOptions` se arman desde `local.properties` y, si faltan, la app usa una cuenta local | El proyecto compila y se puede demostrar sin credenciales; con ellas se activan Auth y Firestore. |
| N-008 | applicationId | `com.flux.notes` en ambas apps, misma firma | La Wearable Data Layer solo conecta apps con el mismo paquete y la misma firma. |
| N-009 | Configuración | URL del servidor, timeouts e IDs de Firebase en `BuildConfig` desde `local.properties` (plantilla `.example`) | Cero valores quemados y ningún secreto en el repositorio. |
| N-010 | Calidad | ktlint (estilo `android_studio`) y detekt con overrides en `config/detekt/detekt.yml` | Estilo uniforme y detección de olores de código en CI. |
| N-011 | Pruebas de UI | Compose UI tests con Robolectric en la JVM | Corren sin emulador (no hay espacio en disco para AVD) y también en CI. |
| N-012 | Errores | `AppError` sellado + `FluxResult` | Ninguna excepción llega a la UI; cada error se traduce a strings.xml. |
| N-013 | Clasificación | `ClassifyNoteUseCase` recorre estrategias en orden (remota → local) | Patrón Strategy con fallback automático; la local nunca falla. |
| N-014 | Fechas | El parser determinista en español tiene prioridad sobre la fecha del modelo | La aritmética de fechas relativas de un LLM no es confiable. |
| N-015 | Desempate de categorías | Específicas antes que genéricas: Deberes, Compras, Salud, Comidas, Tareas, Recordatorios, Ideas, Información | "Comprar pan el viernes" es Compras con fecha, no un Recordatorio genérico. |
| N-016 | Contexto de la IA | Al servidor solo viajan ocupación y horarios, no nombre ni edad | Minimización de datos personales. |
| N-018 | KSP | `ksp.useKSP2=false` (KSP1) | KSP2 2.2.20-2.0.3 no resolvía clases de los módulos Kotlin/JVM al generar código de Hilt en `:mobile`; KSP1 sí. |
| N-019 | Onboarding | Un solo destino con el paso persistido en DataStore (sin pila de navegación) | Si se cierra la app, el onboarding continúa donde quedó; "Atrás" y el botón del sistema vuelven al paso anterior. |
| N-020 | Cuenta local | Sin Firebase configurado se ofrece "Continuar en modo local" | La app se puede usar y demostrar sin credenciales; con Firebase se exige cuenta real. |
| N-021 | Transferencia | Correo: reautenticación con contraseña; Google: el inicio de sesión recién hecho cuenta como reciente | Evita que alguien con una sesión vieja robe la cuenta desde otro celular. |
| N-017 | Vinculación | `PhoneOwnershipPolicy` y `WatchLinkPolicy` son funciones puras del dominio | La regla "un celular y un reloj por cuenta" se prueba sin Firebase ni dispositivos. |
