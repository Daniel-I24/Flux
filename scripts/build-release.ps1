# Runs the full quality gate and builds signed, R8-minified release APKs for the
# phone and the watch into dist/. Requires JDK 21 and a configured keystore.
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
Set-Location $root
if (-not (Select-String -Path local.properties -Pattern "^flux.keystore.path=.+" -Quiet)) {
    throw "Falta la firma: ejecuta scripts/create-keystore.ps1 y completa local.properties."
}
.\gradlew.bat ktlintCheck detekt testDebugUnitTest :core:model:test :core:domain:test :core:ai:test :mobile:assembleRelease :wear:assembleRelease
if ($LASTEXITCODE -ne 0) { throw "La compilación falló." }
New-Item -ItemType Directory -Force dist | Out-Null
Copy-Item mobile/build/outputs/apk/release/mobile-release.apk "dist/flux-mobile.apk" -Force
Copy-Item wear/build/outputs/apk/release/wear-release.apk "dist/flux-wear.apk" -Force
Get-ChildItem dist/*.apk | Select-Object Name, @{n = 'MB'; e = { [math]::Round($_.Length / 1MB, 2) } }
