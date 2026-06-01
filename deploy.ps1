# E-Commerce Platform — Docker Compose helper (Windows PowerShell)
# Usage:
#   .\deploy.ps1              # build + start (uses override unless -Production)
#   .\deploy.ps1 -BuildOnly
#   .\deploy.ps1 -StartOnly
#   .\deploy.ps1 -Down
#   .\deploy.ps1 -Status

param(
    [switch]$Production,
    [switch]$BuildOnly,
    [switch]$StartOnly,
    [switch]$Down,
    [switch]$Status
)

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

$composeArgs = @("-f", "docker-compose.yml")
if (-not $Production -and (Test-Path "docker-compose.override.yml")) {
    $composeArgs += @("-f", "docker-compose.override.yml")
}

if (-not (Test-Path ".env")) {
    if (Test-Path ".env.example") {
        Copy-Item ".env.example" ".env"
        Write-Warning ".env created from .env.example — set JWT_SECRET and passwords before production."
    } else {
        throw ".env.example is missing"
    }
}

function Invoke-Compose {
    param([string[]]$Args)
    & docker compose @composeArgs @Args
    if ($LASTEXITCODE -ne 0) { throw "docker compose failed ($LASTEXITCODE)" }
}

if ($Status) {
    Invoke-Compose @("ps")
    exit 0
}

if ($Down) {
    Invoke-Compose @("down", "--remove-orphans")
    exit 0
}

if (-not $StartOnly) {
    Write-Host "[STEP] Building images..."
    Invoke-Compose @("build", "--parallel")
}

if ($BuildOnly) { exit 0 }

Write-Host "[STEP] Starting infrastructure..."
Invoke-Compose @("up", "-d", "mysql", "redis", "rabbitmq", "elasticsearch", "nacos", "zipkin")

Write-Host "[STEP] Starting backend..."
Invoke-Compose @("up", "-d", "gateway", "shop-bff", "product-service", "user-service", "cart-service", "order-service")

Write-Host "[STEP] Starting frontend..."
Invoke-Compose @("up", "-d", "frontend")

Write-Host "[INFO] Catalog data is seeded by product-service on startup."
$frontendPort = if ($env:FRONTEND_PORT) { $env:FRONTEND_PORT } else { "80" }
Write-Host "[INFO] Frontend: http://localhost:$frontendPort"
Write-Host "[INFO] API (via Nginx): http://localhost/api/"
