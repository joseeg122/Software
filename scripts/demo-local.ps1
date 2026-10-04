# FinTrack 360 — demo local SIN Docker (requiere JDK 21+, Maven y Node 20+ en el PATH).
# El backend usa un PostgreSQL embebido y temporal: los datos se reinician en cada arranque.
# La contraseña temporal del usuario "analista" aparece en la ventana del backend.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

Start-Process powershell -ArgumentList '-NoExit', '-Command', "Set-Location '$root\backend'; mvn spring-boot:test-run"
Set-Location (Join-Path $root 'frontend')
if (-not (Test-Path 'node_modules')) { npm install }
npm run dev
