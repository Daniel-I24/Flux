# Emuladores: celular + reloj Wear OS

1. Android Studio > Device Manager > crea:
   - **Pixel 8** con imagen *Google Play* (API 35 o superior).
   - **Wear OS Large Round** con imagen *Wear OS 5* (API 34) o superior.
2. Arranca ambos. En el emulador del celular abre la app **Wear OS by Google / Pixel Watch** (Play Store) o usa
   Device Manager > menú del reloj > **Pair Wearable** y elige el celular.
3. Compila e instala: `./gradlew :mobile:assembleDebug :wear:assembleDebug` y luego
   `scripts/install.ps1 -Phone emulator-5554 -Watch emulator-5556 -Debug`.
4. Lista de verificación:
   - [ ] Onboarding completo en el celular; en el paso 6 aparece el reloj y se confirma el código en el reloj.
   - [ ] En el reloj: *Dictar nota* (en el emulador escribe el texto), vista previa con cuenta regresiva, se guarda.
   - [ ] La nota aparece en *Notas* del celular con su categoría.
   - [ ] Ajustes > Modo demostración > *Cargar ejemplos*: la alarma suena a los 10 s en ambos; *Posponer* en uno quita la notificación del otro.
   - [ ] Apaga el Bluetooth/conexión del reloj, dicta una nota: queda en cola y llega al reconectar.
   - [ ] Tile *Dictar nota* y complicación *Próximo recordatorio* muestran el siguiente recordatorio.
