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
| N-017 | Vinculación | `PhoneOwnershipPolicy` y `WatchLinkPolicy` son funciones puras del dominio | La regla "un celular y un reloj por cuenta" se prueba sin Firebase ni dispositivos. |
| N-018 | KSP | `ksp.useKSP2=false` (KSP1) | KSP2 2.2.20-2.0.3 no resolvía clases de los módulos Kotlin/JVM al generar código de Hilt en `:mobile`; KSP1 sí. |
| N-019 | Onboarding | Un solo destino con el paso persistido en DataStore (sin pila de navegación) | Si se cierra la app, el onboarding continúa donde quedó; "Atrás" y el botón del sistema vuelven al paso anterior. |
| N-020 | Cuenta local | Sin Firebase configurado se ofrece "Continuar en modo local" | La app se puede usar y demostrar sin credenciales; con Firebase se exige cuenta real. |
| N-021 | Transferencia | Correo: reautenticación con contraseña; Google: el inicio de sesión recién hecho cuenta como reciente | Evita que alguien con una sesión vieja robe la cuenta desde otro celular. |
| N-022 | Protocolo | JSON versionado (`SyncProtocol.VERSION`) con discriminador `type`, validación de campos y deduplicación por id | Mensajes inválidos o de otra versión se descartan; una versión distinta muestra "Actualiza FLUX". |
| N-023 | Canales Data Layer | MessageClient para comandos (con correlación request/response) y DataClient para el snapshot (agenda, categorías, ajustes) | DataClient sincroniza el estado al reconectar sin polling; los comandos son puntuales. |
| N-024 | Recepción de mensajes | `SyncInbox.receive()` devuelve el mensaje en vez de emitirlo en un flujo | Si el servicio despierta la app, un SharedFlow sin suscriptores perdería el mensaje. |
| N-025 | Identidad en el reloj | El reloj guarda un `accountId` = SHA-256 del uid, nunca el uid | Privacidad: el reloj no necesita el identificador real de Firebase. |
| N-026 | Cola offline del reloj | Room `outbox` + WorkManager con backoff; se borra al recibir `NoteAck` | Una nota dictada sin celular llega exactamente una vez (el celular fusiona por id). |
| N-027 | Clasificación en el reloj | Primero pide al celular (timeout `PHONE_REPLY_TIMEOUT_MS`), si no, clasificador local | El celular tiene IA remota, perfil y categorías; el usuario nunca queda bloqueado. |
| N-028 | Reclasificación | Notas clasificadas localmente se reclasifican con la IA remota vía WorkManager (red requerida), sin tocar correcciones del usuario | "Si mejora el resultado" sin polling y respetando la decisión del usuario. |
| N-029 | Alarmas | `AndroidAlarmScheduler` compartido + `AlarmTarget` por app; inexactas si no hay permiso de alarmas exactas | Nunca se pierde un recordatorio; cada app muestra su propia UI de alarma. |
| N-030 | Sonidos | Tonos WAV generados por script + tonos del sistema; un canal de notificación por sonido | Android fija el sonido por canal; así cada categoría puede sonar distinto. |
| N-031 | Servidor | Express sin Socket.IO; `/api/classify` exige ID token de Firebase (firebase-admin) y App Check opcional; límite por usuario | El tráfico reloj-celular va por Data Layer; el servidor solo clasifica. |
| N-032 | Credenciales del servidor | Cuenta de servicio en `FIREBASE_SERVICE_ACCOUNT_BASE64` (variable de Render) | Ningún archivo de credenciales en el repositorio ni en el disco del servidor. |
| N-033 | Reglas de Firestore | Solo `users/{uid}` con campos y tamaños validados; todo lo demás cerrado; probadas con el emulador | Las notas nunca salen del celular; Firestore solo guarda la vinculación. |
| N-034 | UI del reloj | `TransformingLazyColumn` para listas (scroll con corona) y `Column` con scroll en la vista previa | La vista previa es corta y debe verse completa en pantallas pequeñas y redondas. |
| N-035 | Sonido por recordatorio | Prioridad: sonido de la nota → de la categoría → predeterminado (`ReminderSoundPolicy`) | Una regla pura y probada, usada igual en celular y reloj. |
| N-036 | Migraciones | Room `version = 2` con `AutoMigration(1→2)` y esquemas exportados en `core/data/schemas` | Agregar una columna nula no borra las notas de quien ya instaló la app. |
| N-037 | Vibración en el celular | El canal se identifica por (sonido, patrón de vibración) | Android congela la vibración del canal; cambiarla en Ajustes debe tener efecto. |
| N-038 | Notificación sincronizada | Al reprogramar (posponer) una alarma se retira su notificación visible en ese dispositivo | Posponer en el reloj quita también la notificación del celular y viceversa. |
| N-039 | Tile y complicación | ProtoLayout Material 3 + `SuspendingComplicationDataSourceService`; se refrescan al aplicar el snapshot o al completar/posponer | Siempre muestran el próximo recordatorio real sin polling (solo un respaldo de 15 min). |
| N-040 | Navegación del celular | Cuatro pestañas (`NavigationBar`) con estado `rememberSaveable` y subpáginas internas en Ajustes | KISS: no hace falta un grafo de navegación para cuatro destinos y dos formularios. |
| N-041 | Notas escritas en el celular | `CaptureNoteUseCase`: misma clasificación que el reloj y reclasificación remota en cola (`ReclassificationQueue`) | Un solo flujo de captura; el dominio no conoce WorkManager. |
| N-042 | Idioma | La app es solo en español; se eliminaron `languageTag` y `values-en` | Los requisitos piden textos en español; un idioma parcial mezclaría inglés y español. |
| N-043 | Permisos | Se quitó `RECORD_AUDIO` del celular | El dictado ocurre en el reloj (reconocedor del sistema); el celular no graba audio. |
| N-044 | Modo demostración | 5 notas ya clasificadas con ids `demo-N` (cargar dos veces reemplaza; "Quitar ejemplos" solo borra esas) y una alarma a los 10 s | Demostrable sin IA ni red, sin ensuciar las notas del usuario. |
| N-045 | Revisión de seguridad | `allowBackup=false` + `dataExtractionRules` en ambas apps, HTTPS obligatorio, PendingIntents inmutables, receptores no exportados, sin logs en release | Las notas, el perfil y las llaves nunca salen del dispositivo por copias o transferencias. |
| N-046 | Dependencias | Se eliminaron del catálogo `security-crypto`, `ui-graphics`, `hilt-android-testing`, `work-testing` y `test-ext-junit`; `firebase-admin` 14 (0 vulnerabilidades en `npm audit --omit=dev`) | Nada sin uso; el cifrado usa el Android Keystore directamente (`KeystoreDataCipher`). |
| N-047 | Rendimiento | R8 + recursos reducidos en release (celular 4,3 MB, reloj 4,6 MB); sin Baseline Profile | Generarlo exige un dispositivo físico o emulador con el módulo `baselineprofile`; queda como mejora futura. |

