# Creates the release keystore OUTSIDE git (keystore/ is ignored) and prints the
# lines to add to local.properties. Keep the file and the passwords safe: Play
# Store updates must be signed with the same key forever.
param(
    [string]$Alias = "flux",
    [string]$Path = "keystore/flux-release.jks"
)
$ErrorActionPreference = "Stop"
$root = Split-Path $PSScriptRoot -Parent
$target = Join-Path $root $Path
if (Test-Path $target) { throw "Ya existe $target. No se sobrescribe." }
New-Item -ItemType Directory -Force (Split-Path $target) | Out-Null
$keytool = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME "bin\keytool.exe" } else { "keytool" }
& $keytool -genkeypair -v -keystore $target -alias $Alias -keyalg RSA -keysize 4096 -validity 10000
Write-Host ""
Write-Host "Agrega a local.properties (no se sube a git):"
Write-Host "flux.keystore.path=$($Path -replace '\','/')"
Write-Host "flux.keystore.alias=$Alias"
Write-Host "flux.keystore.password=<la contraseña del almacén>"
Write-Host "flux.keystore.keyPassword=<la contraseña de la llave>"
