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
| N-030 | Sonidos | (El canal por sonido fue reemplazado por N-055) Tonos WAV generados por script + tonos del sistema; un canal de notificación por sonido | Android fija el sonido por canal; así cada categoría puede sonar distinto. |
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
| N-043 | Permisos | (Reemplazada por N-060) Se quitó `RECORD_AUDIO` del celular | El dictado ocurre en el reloj (reconocedor del sistema); el celular no graba audio. |
| N-044 | Modo demostración | 5 notas ya clasificadas con ids `demo-N` (cargar dos veces reemplaza; "Quitar ejemplos" solo borra esas) y una alarma a los 10 s | Demostrable sin IA ni red, sin ensuciar las notas del usuario. |
| N-045 | Revisión de seguridad | `allowBackup=false` + `dataExtractionRules` en ambas apps, HTTPS obligatorio, PendingIntents inmutables, receptores no exportados, sin logs en release | Las notas, el perfil y las llaves nunca salen del dispositivo por copias o transferencias. |
| N-046 | Dependencias | Se eliminaron del catálogo `security-crypto`, `ui-graphics`, `hilt-android-testing`, `work-testing` y `test-ext-junit`; `firebase-admin` 14 (0 vulnerabilidades en `npm audit --omit=dev`) | Nada sin uso; el cifrado usa el Android Keystore directamente (`KeystoreDataCipher`). |
| N-047 | Rendimiento | R8 + recursos reducidos en release (celular 4,3 MB, reloj 4,6 MB); sin Baseline Profile | Generarlo exige un dispositivo físico o emulador con el módulo `baselineprofile`; queda como mejora futura. |
| N-048 | Formato de fechas | `FluxLocale.SPANISH` para días y horas (24 h) en ambas apps | En un dispositivo en inglés aparecían "Mon/Tue" y "AM/PM" junto a textos en español (hallado en el emulador). |
| N-049 | Horas ambiguas | "a las 1–5" sin "am/pm/de la mañana/madrugada" se interpreta como de la tarde | "Cita con el dentista a las 3" debe sonar a las 15:00, no a las 3:00 (hallado en el emulador). |
| N-050 | App Check en el servidor | `APP_CHECK_REQUIRED=false` en Render y en `render.yaml` | Los APK se instalan fuera de Play Store y Play Integrity no puede certificarlos. Cada llamada sigue exigiendo el ID token de Firebase y tiene límites por usuario e IP. Pasar a `true` al publicar en Play Store. |
| N-051 | Configuración de Firebase | Los valores de `google-services.json` (apiKey, appId, projectId, web client) se copian a `local.properties`; no se usa el plugin `google-services` | Un solo origen de configuración, ignorado por git. El reloj no usa Firebase, así que comparte la misma app Android del proyecto sin configuración propia. |
| N-052 | Modelo de Gemini | `gemini-3.5-flash-lite` con `GEMINI_TIMEOUT_MS=7500` | Desde el 18-sep-2026 los modelos 2.5 solo se sirven a quienes ya los usaban (404 con una llave nueva). `gemini-3.8-flash` daba timeouts, 503 y 429 con la cuota gratuita; `flash-lite` respondió 4/4 en ~2 s. El timeout del servidor (7,5 s) es menor que el de la app (8 s), así que la app nunca espera de más antes de usar la IA local. |
| N-053 | Tarea o Recordatorio | Una acción con fecha es Tarea (o una categoría más específica); Recordatorio es un aviso sin acción concreta. La regla está en `CategoryScorer` y en el prompt del servidor | "Comprar pan mañana" y "recuérdame que hoy juega Colombia" no son lo mismo; ambos clasificadores deben decidir igual. |
| N-054 | Ambigüedades | Modelo `Ambiguity` y `ResolveAmbiguityUseCase` compartidos: chips en el celular, botones grandes en el reloj. Con preguntas la cuenta regresiva se detiene. El servidor agrega `alternativeCategoryIds`, opcional y filtrado contra las categorías enviadas | Una sola regla de negocio en ambas apps; un campo opcional no rompe a los clientes v1.0 ni el esquema zod. |
| N-055 | Canales de alarma | Los canales son silenciosos y el sonido lo reproduce `AlarmRinger`. Los canales v1.0 (uno por sonido) se borran una vez | Android congela el sonido del canal y no admite archivos propios por recordatorio; así cada nota suena con su tono. |
| N-056 | Servicio de alarma | Foreground service `mediaPlayback` con `USAGE_ALARM` en bucle hasta la acción del usuario o `alarmDurationSeconds` (60 s por defecto), luego queda como perdida | Es el tipo de servicio que corresponde a reproducir un tono; el tiempo límite evita que suene sin fin. |
| N-057 | Paleta "Aurora" | Violeta, laguna y brasa con neutros "Ink"; las pantallas solo leen roles del tema. Contraste calculado abajo con la fórmula WCAG 2.1: todos los pares de texto ≥ 4,5:1 | Identidad propia y accesibilidad comprobable. |
| N-058 | Vencidos en tiempo real | `ObserveNoteSectionsUseCase` reevalúa al cambiar las notas y justo cuando vence la siguiente (con tope de 60 s por si cambia el reloj del sistema). Agenda, Inicio y snapshot usan este flujo | "Próximo" y "Vencidos" se actualizan sin reabrir la app y sin sondeo continuo. |
| N-059 | Tono propio | `ACTION_OPEN_DOCUMENT` (`audio/*`), copia en almacenamiento interno y `SoundRef.Custom`. Si falta o es inválido, suena el tono de respaldo. No viaja al reloj | El URI del selector puede caducar; el reloj no tiene el archivo y v1.0 no puede decodificarlo. Limitación conocida: el archivo de un tono reemplazado permanece en almacenamiento interno. |
| N-060 | Micrófono en el celular | Se restaura `RECORD_AUDIO`, pedido solo al tocar el micrófono; sin él o sin reconocedor se usa `RecognizerIntent` o la escritura | v1.1 agrega dictado en el celular (reemplaza N-043). |
| N-061 | Dictado en el reloj | `VoiceRecognizer` con `SpeechRecognizer` es-CO, respaldo es-ES y `RecognizerIntent` si no hay servicio. La escritura solo existe en debug | Reconocer dentro de la app permite la lava reactiva y errores claros con reintento. |
| N-062 | zod 4 en el servidor | Se actualiza a zod 4.6.7 y dependencias: `npm audit --omit=dev` queda en 0 | Había vulnerabilidades; el esquema se adaptó (`.default([])` en `CORS_ORIGINS`). |
| N-063 | Agenda atómica en el reloj | `NoteRepository.replaceAll` en una transacción; `mergeAgenda` conserva lo que la cola aún no entregó | Borrar y volver a insertar cerraba la alerta que sonaba, y un snapshot deshacía cambios hechos en el reloj sin red. |
| N-064 | Recordatorios perdidos | Solo `BOOT_COMPLETED` vuelve a sonar los de las últimas 6 h; `MY_PACKAGE_REPLACED` y el cambio de hora solo reprograman los futuros | Actualizar la app no debe hacer sonar recordatorios antiguos. |
| N-065 | Compatibilidad del protocolo | `SyncProtocol.VERSION` sigue en 1: los campos nuevos son opcionales y se ignoran los desconocidos. `phoneAppVersion` permite avisar si las apps difieren | Celular v1.1 con reloj v1.0 (y al revés) siguen funcionando. |
| N-066 | Guardia de cuenta | `AccountGuard` se pausa durante un inicio de sesión; si otra instalación toma la cuenta, cierra la sesión y lo explica | En v1.0 el celular nuevo cerraba su propia sesión con el diálogo de transferencia abierto, y la transferencia fallaba. |
| N-067 | Trabajo de sincronización | `SyncWorkScope` singleton en lugar de un scope cancelado en `onDestroy` del listener | Play services destruye el servicio tras entregar el evento; una clasificación de varios segundos o un ack no deben cancelarse. |
| N-068 | Orden de snapshots | `SnapshotApplier` es singleton, aplica uno a la vez y descarta los anteriores por `generatedAt` | El snapshot leído al arrancar podía competir con uno nuevo y dejar datos viejos. |
| N-069 | Versión desplegada | `/api/health` devuelve `release` (`RENDER_GIT_COMMIT`) | Permite confirmar qué commit sirve Render; un hash de git no es un secreto. |
| N-070 | Límite de 150 líneas | `scripts/check-line-limit.mjs` en la CI; excluye generados, lockfiles, XML, documentación y catálogos | Una regla sin verificación automática se incumple sin que nadie lo note. |
| N-071 | API 26 | `AlarmActivity` usa las banderas de ventana en Android 8.0 | `setShowWhenLocked` existe desde 8.1; el lint detectó un cierre inesperado al sonar la alarma. |
| N-072 | KSP | Se mantiene KSP1 (N-018) | KSP2 falló con los módulos JVM puros; cambiar antes de entregar agrega riesgo sin beneficio para el usuario. |
| N-073 | Alarma atendida | `ActiveAlarm.awaitHandled` devuelve un booleano explícito | `first { it != noteId }` devolvía `null` al detener la alarma, igual que `withTimeoutOrNull` al agotarse el tiempo: un recordatorio completado o pospuesto desde la notificación quedaba además como "perdido" (hallado en el emulador con el APK release). |
| N-074 | Fin del tutorial del reloj | Se guarda `tutorialDone` y solo entonces se sale; Inicio decide leyendo el almacén, no `uiState` | Tras 5 s en el tutorial, `WhileSubscribed` detiene el flujo de Inicio y su último valor seguía en `false`: "Omitir" y "Empezar" reabrían el tutorial hasta matar la app (hallado en el emulador con el APK release, 6 de 6 intentos). |
| N-075 | Registros FluxLink | `LinkLog` escribe con `Log.println(INFO)` bajo la etiqueta `FluxLink`, también en release; solo estados y conteos | N-045 quita los logs de release, pero la vinculación falla en relojes reales donde no hay depurador. R8 elimina `Log.v/d/i`, no `println`. Nunca se registran ids de nodo, nombres, códigos ni cuentas. |
| N-076 | Diagnóstico del reloj | Solo un nodo cercano (`isNearby`) cuenta como celular emparejado; la regla vive en `ConnectionDiagnosis` (pura, probada). Cada causa tiene etiqueta, mensaje y acción propios; el aviso va arriba del micrófono. "Vincular" abre FLUX en el celular con `flux://link?watch=<nodo>` (`RemoteActivityHelper`) | Un Galaxy Watch sin emparejar ve nodos remotos y decía "Instala FLUX en el celular", cortado en el borde redondo. El reloj debía ofrecer la vinculación en cuanto detecta el celular, sin depender de que el usuario la busque en el celular. |
| N-077 | Ajustes > Reloj | Sección siempre visible con Vincular reloj, Cambiar de reloj y Desvincular, y un selector con todos los relojes emparejados. El reloj de la cuenta sigue "vinculado" aunque esté lejos; al cambiar, el anterior recibe `Unlink`. Reglas en `WatchLinkRules` (pura, probada) | Tras desvincular el reloj del emulador no había forma de vincular otro: el panel solo mostraba una acción según el estado. |
| N-078 | Selector de tonos propio | Hoja de FLUX con una sola lista: Agregar desde archivos, tonos de FLUX, tonos del teléfono (`RingtoneManager`) y tonos importados, cada uno con play/stop; los importados se renombran o eliminan (`tones.json` junto a las copias). Se acepta lo que `MediaExtractor` decodifica como audio, sin importar el tipo MIME; los audios de WhatsApp se nombran por su fecha. La vista previa usa `MediaPlayer` | El selector del sistema del fabricante rechazaba tonos propios ("No se puede agregar el tono personalizado"). `Ringtone` puede reproducir fuera del proceso y no lee la copia privada, por eso el play no sonaba con un audio importado. Los .opus de WhatsApp llegan como `application/ogg` u `octet-stream` y se rechazaban. Reemplaza el selector del sistema de N-059. |
| N-079 | Ajustes en 360 dp | Duración de la alarma con etiquetas cortas (30 s, 1 min, 2 min, 5 min), segmentos iguales sin ícono de verificación y una sola línea; las filas de botones bajan de línea (`FlowRow`) | "30 segundos" se partía en varias líneas y deformaba el botón con texto grande. |
| N-080 | Pantallas del reloj | Cada pantalla tiene un contenido sin estado (`AgendaContent`, `OverdueContent`, `CategoriesContent`, `WatchSettingsContent`) con previews en Galaxy Watch 40 mm (396 px) y 44 mm (450 px), Small Round, Large Round y Square; la confirmación del código se desplaza con márgenes según la forma | Sin contenido sin estado no había previews de Ajustes, Agenda ni Vencidos, y el código de vinculación usaba un margen fijo que no respeta las pantallas redondas pequeñas. |

## Contraste de la paleta (N-057)

Calculado con la fórmula de luminancia relativa de WCAG 2.1. AA para texto normal exige 4,5:1. El reloj usa los roles del tema oscuro sobre negro.

| Tema | Par | Colores | Contraste |
|---|---|---|---|
| Claro | onPrimary / primary | #FFFFFF / #4C3BCF | 7,4:1 |
| Claro | onPrimaryContainer / primaryContainer | #1B1260 / #E6E1FF | 12,7:1 |
| Claro | onSecondary / secondary | #FFFFFF / #00727D | 5,7:1 |
| Claro | onSecondaryContainer / secondaryContainer | #00363B / #C7F0F2 | 10,8:1 |
| Claro | onTertiary / tertiary | #FFFFFF / #B4401A | 5,7:1 |
| Claro | onTertiaryContainer / tertiaryContainer | #3D1000 / #FFDCCF | 12,9:1 |
| Claro | onBackground / background | #16131F / #F7F5FC | 16,9:1 |
| Claro | onSurface / surface | #16131F / #FFFFFF | 18,3:1 |
| Claro | onSurfaceVariant / surfaceVariant | #4A4560 / #ECE8F5 | 7,5:1 |
| Claro | onError / error | #FFFFFF / #BA1A1A | 6,5:1 |
| Oscuro | onPrimary / primary | #24127A / #C5BBFF | 8,3:1 |
| Oscuro | onPrimaryContainer / primaryContainer | #E6E1FF / #3B2BB0 | 7,7:1 |
| Oscuro | onSecondary / secondary | #00373C / #6ED6DF | 7,7:1 |
| Oscuro | onSecondaryContainer / secondaryContainer | #C7F0F2 / #004F56 | 7,6:1 |
| Oscuro | onTertiary / tertiary | #5B1A00 / #FFB59A | 7,7:1 |
| Oscuro | onTertiaryContainer / tertiaryContainer | #FFDCCF / #8A2E0E | 6,6:1 |
| Oscuro | onBackground / background | #E8E4F3 / #0F0D16 | 15,4:1 |
| Oscuro | onSurface / surface | #E8E4F3 / #16131F | 14,7:1 |
| Oscuro | onSurfaceVariant / surfaceVariant | #C0BAD3 / #2A2638 | 7,8:1 |
| Oscuro | onError / error | #690005 / #FFB4AB | 7,7:1 |
