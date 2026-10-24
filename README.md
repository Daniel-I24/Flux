# FLUX — Notas y recordatorios por voz para Wear OS

FLUX permite dictar una idea, tarea o recordatorio en el reloj. La app la clasifica con IA, detecta la fecha ("mañana a las 8", "el viernes a las 3") y programa la alarma en el reloj y en el celular.

El proyecto tiene tres partes:

| Parte | Tecnología | Qué hace |
|---|---|---|
| `wear/` | Kotlin, Compose for Wear OS | Dictado, vista previa, agenda, tile, complicación, alarmas y cola offline |
| `mobile/` | Kotlin, Compose Material 3 | Cuenta, onboarding, vinculación del reloj, notas, categorías, ajustes y modo demostración |
| `server/` | Node 22, Express, TypeScript | Clasificación con Gemini, protegida con token de Firebase y App Check |

La versión web anterior (simulador en el navegador) se conserva en el tag [`web-simulator-v1`](https://github.com/Daniel-I24/Flux/tree/web-simulator-v1).

## Instalación rápida

1. Descarga `flux-mobile.apk` y `flux-wear.apk` de la sección [Releases](https://github.com/Daniel-I24/Flux/releases).
2. Instala el APK del celular: `adb install flux-mobile.apk`.
3. Instala el APK del reloj (conectado por ADB Wi-Fi): `adb -s <reloj> install flux-wear.apk`.
4. Abre FLUX en el celular y sigue el onboarding. Sin Firebase configurado puedes elegir **Continuar en modo local**.

Ambas apps usan el mismo `applicationId` (`com.flux.notes`) y la misma firma; la Wearable Data Layer lo exige para conectarlas.

**Para una demostración sin reloj ni red:** en el celular, ve a **Ajustes → Modo demostración → Cargar ejemplos**. Se cargan 5 notas ya clasificadas y suena una alarma a los 10 segundos.

## Arquitectura

```
:wear ──┐                         ┌── :core:designsystem (tema, sonidos, locale)
        ├── :core:data ── :core:domain ── :core:model
:mobile ┘   (Room, DataStore)     │
        └── :core:sync            └── :core:ai (clasificadores, parser de fechas)
            (Data Layer)
```

- **`:core:model`**, **`:core:domain`** y **`:core:ai`** son módulos Kotlin/JVM puros: no dependen de Android y se prueban rápido.
- La UI nunca toca los datos directamente: **UI → ViewModel → caso de uso → repositorio**.
- Los errores se modelan con `AppError` sellado y `FluxResult`. Ninguna excepción llega a la pantalla; cada error tiene su texto en `strings.xml`.
- Inyección de dependencias con **Hilt**.

### Flujo de una nota dictada

1. El reloj reconoce la voz con el reconocedor del sistema.
2. Pide la clasificación al celular por la Data Layer (`MessageClient`). Si el celular no responde a tiempo, usa el clasificador local.
3. Muestra la vista previa (categoría, título y fecha) para confirmar o editar.
4. Guarda la nota en Room y la encola en el `outbox`. WorkManager la envía al celular con reintentos y la borra al recibir `NoteAck`.
5. El celular programa la alarma. Si la nota se clasificó localmente, la reclasifica con la IA remota cuando hay red, sin tocar las correcciones del usuario.
6. Completar o posponer en un dispositivo retira la notificación en el otro.

### Patrones de diseño

| Patrón | Dónde | Por qué |
|---|---|---|
| Repository | `NoteRepository`, `CategoryRepository`, `AuthRepository`… (`core/domain/repository`) con implementaciones Room, DataStore y Firebase | El dominio no sabe de dónde vienen los datos. |
| Strategy | `NoteClassifier` con `RemoteNoteClassifier` y `LocalNoteClassifier`; `ClassifyNoteUseCase` las recorre en orden | Si la IA remota falla o no hay red, responde la local. Nunca bloquea al usuario. |
| State | `CaptureState` en el reloj: `Idle`, `Listening`, `Processing`, `Preview`, `Saved`, `Error`. Cada estado decide su transición en `on(event)` | Las transiciones inválidas son imposibles y se prueban sin UI. |
| Observer | `Flow`/`StateFlow` desde Room y DataStore hasta la UI | La pantalla se actualiza sola cuando cambian los datos. |
| Factory | `SyncMessageFactory` (`core/sync`) crea y valida los mensajes del protocolo reloj-celular | Un solo lugar conoce la versión y el formato JSON del protocolo. |
| Adapter | `DataLayerGateway` (Wearable Data Layer), `FirebaseAuthRepository`, `RemoteClassifier` (HTTP) | Adaptan APIs externas a las interfaces del dominio. |

Las decisiones técnicas, con su justificación, están en [`DECISIONS.md`](DECISIONS.md).

## Seguridad y privacidad

- No hay secretos en el repositorio ni en los APK. La configuración viene de `local.properties` (plantilla en `local.properties.example`).
- La llave de Gemini vive solo en el servidor. `/api/classify` exige el token de Firebase del usuario y, en producción, App Check. Además limita las peticiones por usuario y por IP.
- Las notas nunca salen de los dispositivos. Firestore solo guarda la vinculación celular-reloj, con reglas probadas en el emulador (`firebase/tests`).
- Al servidor solo viajan el texto de la nota, la ocupación y los horarios del usuario; nunca el nombre ni la edad.
- El reloj guarda el SHA-256 del uid, no el uid.
- `allowBackup=false`, HTTPS obligatorio, PendingIntents inmutables, receptores no exportados y sin logs en release.

## Compilar desde el código

Requisitos: JDK 21, Android SDK 36 y Node 22 (solo para el servidor).

```bash
cp local.properties.example local.properties   # y completa los valores
./gradlew ktlintCheck detekt                   # análisis estático
./gradlew testDebugUnitTest :core:model:test :core:domain:test :core:ai:test
./gradlew :mobile:assembleDebug :wear:assembleDebug
```

Para APK firmados (las dos apps con la **misma llave**):

```powershell
scripts/create-keystore.ps1   # crea keystore/flux-release.jks (ignorado por git) y muestra qué agregar a local.properties
scripts/build-release.ps1     # análisis + pruebas + dist/flux-mobile.apk y dist/flux-wear.apk firmados con R8
```

Sin Firebase (`firebase.*` vacíos) la app funciona en modo local: todo queda en el celular y la clasificación es local.

## Firebase (opcional, recomendado)

1. Crea un proyecto en <https://console.firebase.google.com> y agrega una app Android con el paquete `com.flux.notes`. Registra las huellas SHA-256 de tu keystore de release y de debug.
2. **Authentication:** activa *Correo/contraseña* y *Google*. Copia el *Web client ID* de Google a `firebase.webClientId`.
3. Copia `apiKey`, `appId` y `projectId` de *Configuración del proyecto* a `local.properties`. **No** se usa `google-services.json`.
4. **Firestore:** crea la base y publica las reglas, que primero se prueban en el emulador:
   ```powershell
   cd firebase; npm ci; npm test; npx firebase deploy --only firestore:rules --project <tu-proyecto>
   ```
5. **App Check:** registra Play Integrity. En debug se usa el proveedor de depuración: registra en la consola el token que aparece en Logcat.

## Servidor

```bash
cd server
cp .env.example .env   # GEMINI_API_KEY, FIREBASE_PROJECT_ID, FIREBASE_SERVICE_ACCOUNT_BASE64
npm ci
npm run dev            # http://localhost:3001/api/health
npm test
```

Para la cuenta de servicio, descarga el JSON en *Firebase → Configuración → Cuentas de servicio → Generar clave* y conviértelo a base64 (nunca lo subas al repositorio):

```powershell
[Convert]::ToBase64String([IO.File]::ReadAllBytes("service-account.json")) | Set-Clipboard
```

**Despliegue en Render:** *Dashboard → New → Blueprint* y selecciona este repositorio. `render.yaml` crea el servicio `flux-server` y pide los secretos (`GEMINI_API_KEY`, `FIREBASE_PROJECT_ID`, `FIREBASE_SERVICE_ACCOUNT_BASE64`) sin guardarlos en el repositorio. Luego pon la URL del servicio en `flux.apiBaseUrl` y vuelve a compilar.

Sin `GEMINI_API_KEY` el servidor responde 503 en `/api/classify` y las apps clasifican localmente.

## Instalar en emuladores

Sigue [`scripts/emulators.md`](scripts/emulators.md): crear un celular con Google Play y un reloj Wear OS, emparejarlos, instalar con `scripts/install.ps1 -Phone emulator-5554 -Watch emulator-5556 -Debug` y repasar la lista de verificación.

## Instalar en dispositivos reales

1. Activa la depuración USB en el celular y la **depuración por Wi-Fi** en el reloj (*Ajustes → Opciones de desarrollador*). Conecta el reloj con `adb connect <ip>:<puerto>`.
2. Mira los seriales con `adb devices` e instala:
   ```powershell
   scripts/install.ps1 -Phone <serial-celular> -Watch <serial-reloj>          # APK firmados de dist/
   scripts/install.ps1 -Phone <serial-celular> -Watch <serial-reloj> -Debug   # APK de debug
   ```

## Guion de demostración (5 minutos)

1. **Celular:** *Comenzar* → inicia sesión (o *Continuar en modo local*) → nombre, ocupación y rutina → permisos → **vincula el reloj** y confirma en el reloj el código de 6 dígitos.
2. **Reloj:** *Dictar nota* → «**recuérdame llamar a mamá mañana a las 6 de la tarde**». La vista previa muestra *Recordatorios · Mañana 18:00*. Puedes corregir la categoría; se guarda sola en 5 s.
3. **Celular → Notas:** aparece la nota. Busca «mamá», filtra por categoría y ábrela para cambiar la fecha o el sonido (con vista previa).
4. **Celular → Ajustes → Modo demostración → Cargar ejemplos:** a los **10 s** suena «Tomar la pastilla» en el celular y en el reloj. *Posponer* en uno quita la notificación del otro.
5. **Sin conexión:** apaga el Bluetooth del reloj y dicta «comprar pan el viernes». Queda *1 nota por enviar* y llega sola al reconectar.
6. **Reloj:** agrega el tile *Dictar nota* y la complicación *Próximo recordatorio* a la esfera.
7. **Ajustes → Inteligencia artificial:** muestra si se usa Gemini (remota) o el clasificador local.

## Calidad

- **ktlint** y **detekt** en todos los módulos.
- Pruebas unitarias del dominio, del parser de fechas en español, de los clasificadores, del protocolo, de Room y de DataStore; máquina de estados de captura del reloj; pruebas de UI con Compose sobre Robolectric (sin emulador).
- Pruebas del servidor con Vitest y Supertest. Reglas de Firestore probadas con el emulador.
- La CI (`.github/workflows/ci.yml`) corre todo lo anterior en cada push a `main`.
