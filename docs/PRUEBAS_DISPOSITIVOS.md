# FLUX — Pruebas que debes repetir en tus dispositivos

Solo lo que cambió en esta ronda o no se pudo verificar en el emulador, **en este orden**.
Marca *Resultado* con ✅, ❌ (y qué viste) o ⏭ (no se pudo probar).

- **Compila e instala** con `scriptsuild-release.ps1` el commit indicado en el reporte final, y luego los comandos 2 y 4 (`install -r`, **sin desinstalar**).
- **Dispositivos:** celular vivo V2349 y Galaxy Watch4 40 mm (SM-R860, Wear OS 6). FLUX debe funcionar en cualquier reloj con Wear OS 3+ y celular con Android 8+.
- **Antes de empezar:** ejecuta el comando 5a (logs). Al terminar, el 5b, y guarda las capturas con el comando 6.

## 1. Actualización (celular y reloj)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 1.1 | Instala encima en ambos (comandos 2 y 4) | `Success`; tus notas, categorías y ajustes siguen ahí | |
| 1.2 | Abre FLUX en el celular | Tema **Atardecer andino** (terracota). El inicio dice «Hola, …» sobre un degradado | |
| 1.3 | Abre FLUX en el reloj desde el menú de apps | Puntos que giran y forman un anillo, luego el inicio | |

## 2. Permisos (reloj primero: era la causa de que no sonara)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 2.1 | Reloj: Ajustes → **Permisos y alarmas** | Lista con estado: Micrófono, Notificaciones, Pantalla completa, Batería | |
| 2.2 | Toca **Notificaciones** → Permitir | Vuelve y dice «✓ Listo» | |
| 2.3 | Toca **Batería sin restricciones** | Abre Ajustes del reloj (o un aviso). Quita la optimización de FLUX si aparece | |
| 2.4 | Celular: Ajustes → **Permisos** | Pasos numerados con «Concedido», «Pendiente» o «Revisar» | |
| 2.5 | Toca **Permitir** en «Aparecer encima», activa FLUX y vuelve | Cambia solo a «Concedido» | |
| 2.6 | Toca **Permitir** en «Batería sin restricciones» y confirma | Cambia a «Concedido» | |
| 2.7 | vivo: si aparecen «Inicio automático» o «Ventanas emergentes y pantalla de bloqueo», toca **Abrir ajustes** y actívalos | Abre la pantalla de vivo de FLUX | |
| 2.8 | En el reloj (Galaxy Wearable no lo hace solo): Ajustes del reloj → Aplicaciones → FLUX → Batería | Sin restricciones / sin «Suspender» | |

## 3. Alarma en el reloj (crítica)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 3.1 | Reloj: dicta «recuérdame tomar agua en 2 minutos» y deja que se guarde. Deja el reloj en reposo (pantalla apagada) | — | |
| 3.2 | A la hora | El reloj **enciende la pantalla, vibra** y muestra el recordatorio con **Posponer** y **Completar** durante la duración configurada | |
| 3.3 | El celular también suena | Una sola alerta en el reloj (no dos) | |
| 3.4 | Toca **Posponer** en el reloj | Se detienen el reloj **y** el celular | |
| 3.5 | Repite con una nota creada en el celular y toca **Completar** en el celular | Se detienen ambos | |
| 3.6 | Repite 3.1 con el reloj sin uso por 10 min antes de la hora (Samsung lo congela) | Suena igual (en el log: `FluxAlarm: watch alarm due source=local`) | |

## 4. Alarma en el celular (crítica)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 4.1 | Crea «recuérdame estirarme en 2 minutos» y sigue usando otra app (por ejemplo, la cámara) | A la hora aparece **encima** la pantalla de alarma (como una llamada) con Posponer y Completar; suena hasta que actúes o pase el tiempo | |
| 4.2 | Repite con el celular **bloqueado** | Pantalla completa sobre la pantalla de bloqueo | |
| 4.3 | Toca Posponer | Se cierra, deja de sonar y vuelve a sonar 5 min después | |

## 5. Voz en el reloj (crítica)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 5.1 | Dicta «comprar pan mañana a las 8» | Vista previa correcta, nunca «no disponible» | |
| 5.2 | Abre el dictado y quédate callado | «No te escuché, intenta de nuevo» | |
| 5.3 | Categorías → toca una → sal deslizando sin hablar → toca otra y dicta | Escucha normalmente (antes: «no disponible»). La nota queda en la segunda categoría | |
| 5.4 | Repite 5.1 tres veces seguidas rápido | Siempre escucha; en el log nunca `capacity is full` | |
| 5.5 | Modo avión en el reloj y en el celular, dicta | Mensaje de sin conexión o la entrada de voz del sistema; nunca «no disponible» si hay otra opción | |
| 5.6 | Celular: dicta con el micrófono de «Nueva nota» | Escucha y clasifica | |

## 6. Notas que no son notas

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 6.1 | Dicta en el reloj «Hola, ¿cómo estás?» | «Eso no parece una nota o recordatorio. Intenta de nuevo», no guarda | |
| 6.2 | Escribe lo mismo en el celular | Mismo mensaje; el texto queda para corregirlo | |
| 6.3 | Dicta «idea: app para reciclar» | Se guarda como nota (Ideas) | |

## 7. Anticipación, categorías y temas

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 7.1 | Celular: Ajustes → Recordatorios → **Avisarme** → «5 min antes». Crea «llamar a Ana en 7 minutos» | Suena a los 2 minutos (5 antes), en celular y reloj | |
| 7.2 | En la vista previa de otra nota elige «A la hora exacta» | Esa nota suena a su hora | |
| 7.3 | Edita una nota con alarma | Aparece «Avisarme» con «Como en Ajustes (…)» | |
| 7.4 | Celular: Categorías → toca **Compras** → «Nueva nota en Compras» → escribe «llamar al banco» | Se guarda en Compras aunque parezca tarea | |
| 7.5 | Reloj: Categorías | Cada una muestra «N pendientes» | |
| 7.6 | Celular: Ajustes → Tema → **Selva nocturna** y «Oscuro» | Todo verde; los íconos de la barra de estado se ven | |
| 7.7 | Mira el reloj | Cambió a Selva nocturna. Cámbialo en el reloj a Atardecer andino → el celular también cambia | |

## 8. Accesos rápidos (celular)

| # | Paso | Resultado esperado | Resultado |
|---|---|---|---|
| 8.1 | Mantén presionado el ícono de FLUX → **Dictar nota** | Abre FLUX escuchando | |
| 8.2 | Mantén presionado → **Nueva nota** | Abre la hoja para escribir | |
| 8.3 | Panel de ajustes rápidos → editar → agrega **Dictar nota** y tócalo | Abre FLUX escuchando | |
| 8.4 | Reloj: doble pulsación del botón según «Abrir FLUX con el botón» (Permisos y alarmas) | Abre FLUX | |

## Lo que necesito que me entregues

1. Este archivo con la columna *Resultado* llena.
2. `pruebaslux-phone.log` y `pruebaslux-watch.log` (comando 5b), con la hora real del celular y del reloj en que hiciste 3 y 4.
3. Capturas de lo que falle (comando 6).

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
$P = "SERIAL_DEL_CELULAR"; $W = "IP_DEL_RELOJ:PUERTO"; New-Item -ItemType Directory -Force pruebas | Out-Null; & $adb -s $P logcat -c; & $adb -s $W logcat -c; $jp = Start-Job { param($a,$s) & $a -s $s logcat -v time FluxLink:V FluxAi:V FluxAlarm:V SpeechRecognitionManagerServiceImpl:I RecognitionService:I AndroidRuntime:E ActivityManager:W *:S } -ArgumentList $adb,$P; $jw = Start-Job { param($a,$s) & $a -s $s logcat -v time FluxLink:V FluxAi:V FluxAlarm:V SpeechRecognitionManagerServiceImpl:I RecognitionService:I AndroidRuntime:E ActivityManager:W *:S } -ArgumentList $adb,$W; "Capturando… al terminar ejecuta el bloque 5b"
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
