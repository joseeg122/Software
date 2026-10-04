# FinTrack 360 — arranque con Docker Compose.
# Crea docker/.env con secretos aleatorios la primera vez y levanta base de datos, backend y frontend.
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$envFile = Join-Path $root 'docker\.env'

function New-Secret([int]$bytes) {
    $buffer = New-Object byte[] $bytes
    [System.Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($buffer)
    return ([System.BitConverter]::ToString($buffer) -replace '-', '').ToLower()
}

if (-not (Test-Path $envFile)) {
    $lines = @(
        'DB_USER=fintrack',
        "DB_PASSWORD=$(New-Secret 16)",
        "JWT_SECRET=$(New-Secret 32)",
        'APP_DEMO_USER=analista',
        "APP_DEMO_PASSWORD=$(New-Secret 6)"
    )
    [System.IO.File]::WriteAllLines($envFile, $lines)
    Write-Host "Se creó docker\.env con secretos aleatorios."
}

if (-not (Get-Command docker -ErrorAction SilentlyContinue)) {
    Write-Host "Docker no está instalado. Instala Docker Desktop y vuelve a ejecutar este script." -ForegroundColor Yellow
    exit 1
}

docker compose -f (Join-Path $root 'docker\docker-compose.yml') up --build -d
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

$password = (Get-Content $envFile | Where-Object { $_ -like 'APP_DEMO_PASSWORD=*' }) -replace '^APP_DEMO_PASSWORD=', ''
Write-Host ""
Write-Host "FinTrack 360 (PROTOTIPO EDUCATIVO — DATOS COMPLETAMENTE SIMULADOS)"
Write-Host "  Aplicación : http://localhost:5173"
Write-Host "  API        : http://localhost:8080/api"
Write-Host "  Usuario    : analista"
Write-Host "  Contraseña : $password   (solo de demostración, definida en docker\.env)"
