# Installs FLUX on a connected phone and watch (USB or Wi-Fi debugging).
# Usage: scripts/install.ps1 -Phone <serial> -Watch <serial> [-Debug]
# List serials with: adb devices
param(
    [Parameter(Mandatory)] [string]$Phone,
    [Parameter(Mandatory)] [string]$Watch,
    [switch]$Debug
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
if ($Debug) {
    $phoneApk = Join-Path $root "mobile/build/outputs/apk/debug/mobile-debug.apk"
    $watchApk = Join-Path $root "wear/build/outputs/apk/debug/wear-debug.apk"
} else {
    $phoneApk = Join-Path $root "dist/flux-mobile.apk"
    $watchApk = Join-Path $root "dist/flux-wear.apk"
}
# Both apps must share package name AND signature for the Wearable Data Layer to connect them.
adb -s $Phone install -r $phoneApk
adb -s $Watch install -r $watchApk
Write-Host "Listo. Abre FLUX en el celular y sigue el onboarding; en el paso 6 vincula el reloj."
