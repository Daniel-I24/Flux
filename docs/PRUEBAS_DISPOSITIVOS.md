# FLUX v1.1 — Pruebas en dispositivos reales

Una sola sesión, en este orden: **A. Celular → B. Vinculación → C. Reloj → D. Ambos**.
Marca la columna *Resultado* con ✅, ❌ (y qué viste) o ⏭ (no se pudo probar).
Antes de empezar: celular y reloj cargados, Bluetooth y Wi-Fi activos, reloj ya emparejado con el celular desde la app de tu reloj (Galaxy Wearable, Pixel Watch u otra).

Instala APK compilados desde `920d843` (último cambio de código) o posterior: incluyen los arreglos `2ef72a8` (alarma sin barra de título) y `6303618` (mensaje de lista vacía con filtros), y los de compatibilidad (ver la sección **Compatibilidad**).

**Dispositivos de esta prueba.** FLUX debe funcionar en cualquier reloj con Wear OS 3 o superior; este solo es el reloj disponible para probar:

| | Modelo | Sistema | Pantalla |
|---|---|---|---|
| Reloj | Samsung Galaxy Watch4 40 mm (SM-R860) | One UI 8 Watch · Wear OS 6.0 · Android 16 | Redonda |
| Celular | (anótalo) | (anótalo) | — |

Comandos de instalación, logcat y capturas: sección **Comandos** al final.

## A. Celular

### A1. Instalación y actualización

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A1.1 | Antes de instalar, anota cuántas notas y categorías propias tienes y qué tono y vibración usas | — | |
| A1.2 | Instala con `install -r` (comando 2) | `Success`. No pide desinstalar | |
| A1.3 | Ajustes del sistema → Apps → FLUX → Información | Versión **1.1.0** | |
| A1.4 | Abre FLUX | No vuelve a pedir sesión. Aparece el tutorial nuevo (4 pasos) | |
| A1.5 | Compara con A1.1 | Mismas notas, categorías, tono, vibración y minutos de posponer | |

### A2. Tutorial

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A2.1 | Recorre el tutorial con *Siguiente* | Pasos 1 a 4; el último botón dice *Empezar a usar FLUX* | |
| A2.2 | Ajustes → Tutorial → *Ver tutorial*, y toca *Omitir* | Se cierra y no vuelve a salir al reabrir la app | |

### A3. Onboarding (solo si es una instalación nueva; si no, ⏭)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A3.1 | Pasos 1 a 6: sesión, perfil, horarios, permisos y reloj | Cada paso avanza; los permisos muestran *Concedido* | |

### A4. Captura por texto, fechas y ambigüedades

Toca **Nueva nota** y escribe cada frase en el campo de texto; luego toca *Continuar*.

| # | Frase | Resultado esperado | Resultado |
|---|---|---|---|
| A4.1 | `Cita odontológica hoy a las 4:20 p.m` | **Hoy · 16:20**, categoría Salud. Cuenta regresiva "Se guarda en 5 s" y se guarda sola | |
| A4.2 | `clase para mañana a las 7 a.m de Diseño de Interfaces` | **Mañana · 7:00** | |
| A4.3 | `llamar a mamá a las 4 y 20` | 16:20 (de 1 a 5 sin am/pm es la tarde) | |
| A4.4 | `pagar arriendo hoy cuarto para las 5` | 16:45 | |
| A4.5 | `reunión a las 8` | Pregunta **¿A qué hora?** con chips *Hoy · 20:00* / *Mañana · 8:00*. La cuenta regresiva se detiene hasta que eliges | |
| A4.6 | `reunión mañana a las 8` | Mañana 8:00 **sin preguntar** | |
| A4.7 | `recuérdame llamar a mamá` y luego `salir de mi casa a las` (hora incompleta) | Pregunta **¿Cuándo te aviso?** con opciones, *Elegir fecha y hora* y *Sin recordatorio* | |
| A4.8 | `idea para una app de recetas` | Categoría Ideas, sin fecha, sin preguntas | |
| A4.9 | Abre una nota → Editar → Fecha y luego Hora | Selectores en **español** aunque el celular esté en inglés | |

### A5. Captura por voz en el celular

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A5.1 | Ajustes del sistema → Apps → FLUX → Permisos → quita el micrófono | — | |
| A5.2 | Nueva nota → *Dictar con el micrófono* | Pide el permiso **solo ahora** | |
| A5.3 | Concédelo y di "comprar pan mañana a las 8 a. m." | El texto aparece y luego la vista previa *Mañana · 8:00*, Compras | |
| A5.4 | Dicta y quédate en silencio | Mensaje claro y botón para volver a dictar | |

### A6. Secciones, filtros y Vencidos

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A6.1 | Notas | Secciones *Próximos*, *Pendientes*, *Vencidos* y *Completados* con su número | |
| A6.2 | Toca *Tareas* + *Solo pendientes* + busca "reunión" | Solo las tareas pendientes que contienen "reunión" | |
| A6.3 | Cambia a una categoría que no tenga notas con esa búsqueda | **Sin resultados** y "Ninguna nota coincide con tu búsqueda o tus filtros…", sin sugerir crear una nota (`6303618`) | |
| A6.3b | Quita búsqueda y filtros en una cuenta sin notas (o con *Quitar ejemplos*) | *No hay notas aquí* con ejemplos de frases | |
| A6.4 | Crea `prueba vencido hoy a las HH:MM` con la hora de dentro de 2 minutos y **deja la app abierta** en Notas sin tocar nada | Al vencer pasa de *Próximos* a *Vencidos* sin reabrir la app | |
| A6.5 | En Vencidos usa *En 1 hora*, *Reprogramar*, completar (casilla) y eliminar | Cada acción funciona; al eliminar sale *Deshacer* y lo recupera | |
| A6.6 | Marca una nota como completada | Animación del check, vibración corta y *Deshacer* en la barra inferior | |

### A7. Tonos

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A7.1 | Ajustes → Sonido → *Cambiar* | Hoja *Elige un tono*: Tonos de FLUX, Tonos del teléfono, Tus tonos | |
| A7.2 | Toca ▶ en un tono de FLUX y en uno del teléfono | Se escucha la vista previa y se detiene al tocar otra vez | |
| A7.3 | *Agregar desde archivos* → elige un mp3 o un audio de WhatsApp | *Tono importado*; aparece en *Tus tonos* | |
| A7.4 | Renómbralo y elígelo para una categoría | El nuevo nombre se ve en la categoría | |
| A7.5 | Intenta importar un archivo que no sea audio (un PDF) | "No se pudo usar ese archivo…" | |
| A7.6 | Elimina un tono que usa una categoría y deja que suene un recordatorio de esa categoría | Suena el tono de respaldo, sin errores | |

### A8. Alarma en el celular (sin reloj)

Crea `alarma prueba hoy a las HH:MM` con la hora de dentro de 2 minutos.

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A8.1 | Bloquea la pantalla y espera | Se enciende y muestra la alarma a **pantalla completa** sobre el bloqueo, con sonido en bucle a volumen de alarma | |
| A8.2 | Fíjate en la parte superior de esa pantalla | **Sin** barra de título "FLUX": solo la alarma (`2ef72a8`) | |
| A8.3 | Toca *Posponer N min* | Se calla y la nota queda en +N minutos | |
| A8.4 | Cuando vuelva a sonar, toca *Completar* | Se calla y la nota pasa a Completados. **No** aparece "Recordatorio sin atender" | |
| A8.5 | Otra alarma: con la app abierta y la pantalla encendida | Sale como aviso flotante con *Completar* y *Posponer* (comportamiento normal de Android con el celular en uso) | |
| A8.6 | Otra alarma: no la toques | Se calla sola tras *Duración de la alarma* (60 s por defecto) y queda como perdida en *Vencidos* | |
| A8.7 | Ajustes → No molestar de FLUX activo y una alarma dentro de tus horas de sueño | Llega una notificación silenciosa, sin pantalla completa | |
| A8.8 | Programa una alarma para dentro de 5 min y **reinicia el celular** | Después de reiniciar, suena a su hora | |

### A9. Ajustes, IA y sin conexión

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| A9.0 | Ajustes → Permisos → *Si los recordatorios llegan tarde* → *Abrir ajustes de FLUX* | Pasos genéricos de batería e inicio automático; el botón abre la información de FLUX en Ajustes del sistema. Aplica *Sin restricciones* a FLUX y a la app del reloj | |
| A9.1 | Ajustes → Inteligencia artificial | *IA en la nube activa*; si el servidor estaba dormido, primero *Conectando con la IA…* (hasta 1 min) | |
| A9.2 | Activa el modo avión y crea `comprar leche mañana a las 9 a. m.` | Se clasifica igual (Compras, mañana 9:00) con la IA local; Ajustes muestra *IA local* | |
| A9.3 | Quita el modo avión y toca *Comprobar de nuevo* | Vuelve a *IA en la nube activa* | |
| A9.4 | Ajustes → Tema: Claro, Oscuro y Sistema | Toda la app cambia; los textos se leen bien | |
| A9.5 | Ajustes → Avanzado → Modo demostración → *Cargar ejemplos* | 5 notas; una suena a los 10 s (celular y, en D, también el reloj). *Quitar ejemplos* las borra | |
| A9.6 | Opciones de desarrollador → escala de animación 0 (o *Quitar animaciones* en Accesibilidad) | La app no anima listas ni transiciones | |

## B. Vinculación

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| B1 | Instala FLUX en el reloj (comandos 3 y 4) | `Success` | |
| B2 | Celular → Ajustes → Reloj | Muestra el reloj: *Tiene FLUX: listo para vincular* | |
| B3 | Toca *Vincular* | El celular muestra un código; el reloj pregunta *¿Tu celular muestra este código?* | |
| B4 | En el reloj toca *Rechazar* | El celular dice *La vinculación se canceló en el reloj.* | |
| B5 | Vuelve a vincular y toca *Confirmar* | Celular: *Vinculado con …* y *Conectado*. Reloj: *Conectado* en Inicio | |
| B6 | Celular → Ajustes → Reloj → *Desvincular* → confirma, y vuelve a vincular | Se desvincula (reloj: *Sin vincular*) y se vincula otra vez | |
| B7 | En el reloj, con el celular sin sesión (cierra sesión en el celular) | El reloj dice *Celular sin sesión*. Vuelve a iniciar sesión y queda *Conectado* | |

## C. Reloj

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| C1 | Abre FLUX en el reloj por primera vez | Tutorial de 3 pasos: *Dicta con tu voz*, *Revisa y listo*, *Alarmas en ambos*. Con *Omitir* no vuelve a salir | |
| C2 | Inicio del reloj | Hora, estado discreto (*Conectado*), botón grande *Dictar nota*, próximo recordatorio, *Agenda* y *Vencidos* | |
| C3 | *Dictar nota* → di "cita con el dentista mañana a las 4 p. m." | Lava de colores que reacciona a tu voz; luego vista previa *Mañana · 16:00*, Salud, y se guarda sola | |
| C4 | Dicta "reunión a las 8" | Botones grandes *¿A qué hora?*; no se guarda hasta que eliges | |
| C5 | Dicta "recuérdame llamar a mamá" | *¿Cuándo te aviso?* con *Sin recordatorio* | |
| C6 | Dicta y quédate en silencio | *No entendí la frase…* con *Dictar de nuevo*. **Nunca** se guarda ":20" ni "Nota sin título" | |
| C7 | Debe ser solo por voz | No hay botón *Escribir* (solo existe en debug) | |
| C8 | La nota dictada en C3 | Aparece en el celular en *Próximos* en pocos segundos (*Enviada al celular*) | |
| C9 | Agenda y Vencidos en el reloj | Agenda: Hoy y Próximos. Un recordatorio vencido sale solo en Vencidos, con *En 1 hora*, *Mañana 9:00* y *Eliminar* | |
| C10 | Mantén pulsada la esfera → agrega el tile *Dictar nota*; tócalo | Abre el dictado **directamente** | |
| C11 | Edita la esfera → agrega la complicación *Próximo recordatorio*; tócala | Muestra el próximo recordatorio y abre el dictado | |
| C12 | Configura la doble pulsación del botón con FLUX (Ajustes → Gestos/Botones; la ruta depende del fabricante) | Abre FLUX | |
| C13 | Modo avión **solo en el reloj** (o celular lejos) → dicta una nota | *Se enviará al reconectar*. Al reconectar llega al celular **una sola vez** | |

## D. Ambos dispositivos

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| D1 | Crea en el celular una alarma para dentro de 2 min | Suena en el celular y el reloj vibra **al mismo tiempo**; el reloj muestra *Completar* y *Posponer* | |
| D2 | Toca *Posponer* en el **reloj** | Se callan **los dos**; sin notificaciones duplicadas | |
| D3 | Cuando vuelva a sonar, toca *Completar* en el **celular** | Se callan los dos; la nota queda completada en ambos | |
| D4 | Otra alarma: no la toques en ninguno | Ambos paran a los ~60 s y queda en Vencidos en ambos | |
| D5 | Modo demostración (A9.5) con el reloj vinculado | Suena a los 10 s en los dos | |
| D6 | Desde el reloj, completa un recordatorio en la Agenda | Desaparece de Próximos en el celular | |

## Problemas detectados en la revisión previa

1. **Corregido en `2ef72a8`. Barra de título en la alarma del celular.** `AlarmActivity` heredaba el tema del splash de la aplicación, que agrega una barra "FLUX". Ahora usa `Theme.Flux`. Prueba: `AlarmActivityThemeTest`. Comprobarlo en A8.2.
2. **Corregido en `6303618`. Lista vacía con filtros.** Si la búsqueda, una categoría o *Solo pendientes* no dejan notas, la lista dice *Sin resultados* y pide cambiar los filtros. Sin filtros sigue sugiriendo ejemplos. Prueba: `NotesEmptyStateTest`. Comprobarlo en A6.3.
3. **No es un error. Pantalla completa con el celular en uso.** Con la pantalla encendida y desbloqueada, Android muestra la alarma como aviso flotante en vez de pantalla completa (A8.5).

## Compatibilidad

FLUX está hecho para **cualquier** reloj Wear OS 3+ (redondo, cuadrado o rectangular, de cualquier tamaño y marca) emparejado con **cualquier** celular Android 8.0+. El Galaxy Watch4 es solo el reloj de esta prueba.

| | Celular | Reloj |
|---|---|---|
| `minSdk` | 26 · Android 8.0 | 30 · Wear OS 3 |
| `targetSdk` / `compileSdk` | 36 · Android 16 | 36 · Wear OS 6 |
| Hardware obligatorio | Ninguno | `android.hardware.type.watch` |
| Micrófono | Opcional (`required="false"`, `2b7913d`) | Opcional (`required="false"`, `2b7913d`) |

### Qué se verificó y dónde

| Dónde | Qué | Estado |
|---|---|---|
| **Galaxy Watch4 40 mm** (esta sesión) | Instalación con `install -r`, vinculación, dictado real con micrófono, lava, preguntas, alarma con vibración, posponer y completar sincronizados, Vencidos, tile, complicación y doble pulsación (secciones B, C y D) | Pendiente: lo llenas tú en esta sesión |
| **Emulador de celular** (Medium Phone, API 37, 6/11) | Actualización de v1.0.0 a v1.1.0 conservando datos, onboarding, fechas, ambigüedades, alarma a pantalla completa con `USAGE_ALARM` y servicio `mediaPlayback`, posponer, completar, corte a los 60 s, Vencidos en vivo, filtros, tono propio y archivo inválido | Verificado con el APK de `47923f7`; los cambios posteriores se cubren con pruebas automáticas y en esta sesión |
| **Emulador de reloj redondo** (FLUX_Watch, small round, API 37) | Recorrido completo del reloj | **No verificado** en esta ronda |
| **Emulador de reloj cuadrado** | — | **No verificado**: no hay un AVD cuadrado instalado |
| **Vistas previas** (`FluxWearPreviews`) | Small Round, Large Round, Square, Rectangular, Galaxy Watch 40 mm y 44 mm, Pixel Watch 41 mm, y fuente grande (1,24×) en Small Round y Square, aplicadas a todas las pantallas del reloj | Definidas; se revisan en Android Studio, no se renderizan en la CI |
| **Pruebas automáticas** (Robolectric) | `LargeFontShapesTest`: inicio y alarma con fuente 1,24× dentro de la pantalla en redondo de 192 dp y cuadrado de 180 dp. `PreviewUiTest`: vista previa en redondo de 227 dp. `ManifestFeaturesTest`: hardware opcional en ambas apps. `PairingTextsTest`: textos de emparejamiento genéricos | Verificado (`:wear` 36 pruebas, `:mobile` 60, sin fallos) |
| **Android lint** | Sin `NewApi` ni errores en ambas apps. Quedan avisos menores: `InlinedApi` en una constante que `ServiceCompat` ignora antes de Android 10, `UnusedAttribute` de atributos de Android 8.1 y `WearRecents` | Verificado |
| **Revisión de código** | AGSL solo en API 33+ (Canvas antes); vibración con `VibrationAttributes` solo en API 33+ y comprobando `hasVibrator()`; constantes de voz de API 31 como literales para API 30; permisos según versión; voz: `SpeechRecognizer` → `RecognizerIntent` → mensaje claro; sin vibrador la alerta se muestra igual | Verificado |

Para confirmar el hardware opcional en los APK compilados: `& "$env:LOCALAPPDATA\Android\Sdk\build-tools\36.0.0\aapt2.exe" dump badging dist\flux-wear.apk | Select-String "uses-feature"` debe mostrar `uses-feature: name='android.hardware.type.watch'` y `uses-feature-not-required: name='android.hardware.microphone'`.

### Riesgos que dependen de otros fabricantes (no se pueden probar sin su hardware)

1. **Reconocimiento de voz.** Cada reloj trae su propio servicio, o ninguno. La calidad en español y la disponibilidad sin conexión cambian por marca. Si falta, FLUX usa el dictado del sistema o muestra un aviso.
2. **Ahorro de batería del celular.** Xiaomi, Oppo, Vivo, Huawei y Samsung pueden retrasar o matar FLUX o la app del reloj. Mitigación: la ayuda de *Ajustes → Permisos* y la tabla del README; no se puede garantizar en todas las capas.
3. **Doble pulsación del botón.** La asignación depende del fabricante, y algunos relojes no permiten elegir apps de terceros.
4. **Pantallas cuadradas y rectangulares reales.** Solo se cubren con vistas previas y pruebas automáticas; no hay un reloj cuadrado para probar.
5. **Pantalla completa y bloqueo.** Algunas capas limitan las alarmas a pantalla completa o piden un permiso extra. FLUX lo comprueba en *Ajustes → Permisos*.
6. **Wear OS 3 y 4 (API 30–33).** El shader AGSL no existe antes de API 33 y la lava usa Canvas. No hay un reloj con esas versiones para probarlo.
7. **Relojes sin vibrador.** La alerta se ve y suena el celular, pero el reloj no vibra; la app no lo avisa aparte.

## Lo que necesito que me entregues

1. Esta tabla con la columna *Resultado* llena (✅/❌/⏭ y una frase en cada ❌).
2. **Capturas** (comando 6), como mínimo: inicio del celular, Notas con secciones, vista previa con pregunta (A4.5), alarma a pantalla completa (A8.1), hoja de tonos (A7.1), Ajustes → IA, Ajustes → Reloj vinculado (B5), y del reloj: inicio (C2), lava al dictar (C3), vista previa (C3), pregunta (C4), Agenda, Vencidos, tile y complicación, alarma (D1).
3. **Logs** (comando 5): los archivos `flux-phone.log`, `flux-watch.log`, `flux-phone-crash.log` y `flux-watch-crash.log` de la sesión completa. Llevan las etiquetas `FluxLink` (vinculación y sincronización) y `FluxAi` (estado del servidor), además de los cierres inesperados. No contienen correos, códigos ni identificadores.
4. **Datos del celular**: marca, modelo y versión de Android. Los del reloj ya están registrados arriba (Galaxy Watch4 40 mm); el comando 7 sirve para otro reloj.
5. Para cada ❌: la hora aproximada en que pasó, para ubicarla en el log.

## Comandos (PowerShell)

Pon tus datos en la primera línea de cada bloque. `$P` es el serial del celular (`adb devices` con el cable USB) y `$W` es `IP:PUERTO` del reloj.

```powershell
# 1) Preparar: ruta de adb y variables
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"; Set-Location C:\Users\ivan2\Projects\flux-app; & $adb devices
```

```powershell
# 2) Celular por USB: actualizar sin borrar datos (nunca uses uninstall)
$P = "SERIAL_DEL_CELULAR"; & $adb -s $P install -r dist\flux-mobile.apk; & $adb -s $P shell dumpsys package com.flux.notes | Select-String "versionName|versionCode"
```

```powershell
# 3) Reloj: en el reloj ve a Ajustes > Opciones de desarrollador > Depuración inalámbrica > "Vincular nuevo dispositivo".
#    Usa la IP:PUERTO y el código de 6 dígitos de esa ventana (el puerto de vincular es distinto del de conectar).
& $adb pair IP_DEL_RELOJ:PUERTO_DE_VINCULACION CODIGO_DE_6_DIGITOS
```

```powershell
# 4) Reloj: conectar (IP:PUERTO que aparece en "Depuración inalámbrica") e instalar sin borrar datos
$W = "IP_DEL_RELOJ:PUERTO"; & $adb connect $W; & $adb -s $W install -r dist\flux-wear.apk; & $adb -s $W shell dumpsys package com.flux.notes | Select-String "versionName|versionCode"
```

```powershell
# 5a) Logs: limpiar y empezar a capturar en segundo plano (deja esta ventana abierta durante las pruebas)
$P = "SERIAL_DEL_CELULAR"; $W = "IP_DEL_RELOJ:PUERTO"; New-Item -ItemType Directory -Force pruebas | Out-Null; & $adb -s $P logcat -c; & $adb -s $W logcat -c; $jp = Start-Job { param($a,$s) & $a -s $s logcat -v time FluxLink:V FluxAi:V AndroidRuntime:E ActivityManager:W *:S } -ArgumentList $adb,$P; $jw = Start-Job { param($a,$s) & $a -s $s logcat -v time FluxLink:V FluxAi:V AndroidRuntime:E ActivityManager:W *:S } -ArgumentList $adb,$W; "Capturando… al terminar ejecuta el bloque 5b"
```

```powershell
# 5b) Logs: guardar todo al terminar (incluye el buffer de fallos)
Receive-Job $jp | Out-File -Encoding utf8 pruebas\flux-phone.log; Receive-Job $jw | Out-File -Encoding utf8 pruebas\flux-watch.log; Stop-Job $jp,$jw; Remove-Job $jp,$jw; & $adb -s $P logcat -d -b crash | Out-File -Encoding utf8 pruebas\flux-phone-crash.log; & $adb -s $W logcat -d -b crash | Out-File -Encoding utf8 pruebas\flux-watch-crash.log; Get-ChildItem pruebas
```

```powershell
# 6) Captura de pantalla: cambia NOMBRE en cada una (celular y reloj). Se guardan en la carpeta pruebas
$n = "NOMBRE"; & $adb -s $P shell screencap -p /sdcard/flux.png; & $adb -s $P pull /sdcard/flux.png "pruebas\cel-$n.png"; & $adb -s $W shell screencap -p /sdcard/flux.png; & $adb -s $W pull /sdcard/flux.png "pruebas\reloj-$n.png"
```

```powershell
# 7) Datos del reloj
& $adb -s $W shell getprop ro.product.manufacturer; & $adb -s $W shell getprop ro.product.model; & $adb -s $W shell getprop ro.build.version.release; & $adb -s $W shell getprop ro.build.version.sdk; & $adb -s $W shell getprop ro.cw_build.platform_mr; & $adb -s $W shell wm size; & $adb -s $W shell wm density
```

```powershell
# 8) Al terminar: desconectar y desactivar la depuración del reloj (luego confírmalo en Ajustes > Opciones de desarrollador)
& $adb -s $W shell settings put global adb_wifi_enabled 0; & $adb disconnect $W; & $adb kill-server
```
