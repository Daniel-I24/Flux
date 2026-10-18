<#
.SYNOPSIS
  Prepares an Android emulator (or a USB phone) to test FLUX as a secure context.

.DESCRIPTION
  1. Starts the AVD if no emulator is running (skipped with -Usb).
  2. Waits for Android to finish booting.
  3. Runs `adb reverse` for the web (5173) and API (3001) ports, so Chrome on the
     device opens http://localhost:5173 - localhost counts as a secure context, which
     the microphone, notifications and the service worker require.
  4. Opens /phone in the device browser.

.EXAMPLE
  npm run emulator
  powershell -ExecutionPolicy Bypass -File scripts/emulator.ps1 -Usb
#>
param(
  [string]$Avd = 'Medium_Phone_API_37.0',
  [int[]]$Ports = @(5173, 3001),
  [string]$Path = '/phone',
  [switch]$Usb,
  [int]$BootTimeoutSeconds = 240,
  # Smaller data partition for machines with little free disk (max 2047 MB). 0 = AVD default.
  [int]$PartitionSizeMb = 2047
)

$ErrorActionPreference = 'Stop'

$sdk = $env:ANDROID_SDK_ROOT
if (-not $sdk) { $sdk = Join-Path $env:LOCALAPPDATA 'Android\Sdk' }
$adb = Join-Path $sdk 'platform-tools\adb.exe'
$emulator = Join-Path $sdk 'emulator\emulator.exe'

if (-not (Test-Path $adb)) { throw "adb not found at $adb. Install Android SDK Platform-Tools or set ANDROID_SDK_ROOT." }

$target = if ($Usb) { '-d' } else { '-e' }

function Invoke-Adb {
  param([Parameter(ValueFromRemainingArguments = $true)][string[]]$Arguments)
  & $adb $target @Arguments
}

$process = $null
if (-not $Usb) {
  $running = (& $adb devices) -match '^emulator-\d+\s+device'
  if (-not $running) {
    if (-not (Test-Path $emulator)) { throw "emulator.exe not found at $emulator." }
    $arguments = @('-avd', $Avd, '-netdelay', 'none', '-netspeed', 'full', '-no-snapshot-save')
    if ($PartitionSizeMb -gt 0) { $arguments += @('-partition-size', "$PartitionSizeMb") }
    Write-Host "Starting AVD $Avd ..."
    $process = Start-Process -FilePath $emulator -ArgumentList $arguments -PassThru
  }
}

Write-Host 'Waiting for the device to boot...'
$deadline = (Get-Date).AddSeconds($BootTimeoutSeconds)
$booted = ''
do {
  Start-Sleep -Seconds 3
  if ($process -and $process.HasExited) {
    throw "The emulator exited (code $($process.ExitCode)). Check free disk space on C: and run it from Android Studio to see the error."
  }
  $booted = (& $adb $target shell getprop sys.boot_completed 2>$null | Out-String).Trim()
} until ($booted -eq '1' -or (Get-Date) -gt $deadline)
if ($booted -ne '1') { throw 'The device did not finish booting in time. With -Usb, check that USB debugging is enabled and authorized.' }

foreach ($port in $Ports) {
  Invoke-Adb reverse "tcp:$port" "tcp:$port" | Out-Null
  Write-Host "adb reverse tcp:$port -> localhost:$port"
}

$url = "http://localhost:$($Ports[0])$Path"
Invoke-Adb shell am start -a android.intent.action.VIEW -d $url | Out-Null
Write-Host "Opened $url on the device."
Write-Host ''
Write-Host 'Checklist:'
Write-Host '  - Run "npm run dev" on this computer (web + server).'
Write-Host '  - Microphone (emulator): Extended controls (...) > Microphone > enable "Virtual microphone uses host audio input".'
Write-Host '  - Pair: open http://localhost:5173/watch on this computer and type its 6-character code in the phone (link icon).'
Write-Host '  - Install the PWA: Chrome menu > "Add to Home screen" / "Install app".'
