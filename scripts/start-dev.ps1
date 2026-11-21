<#
.SYNOPSIS
    Inicializa o ambiente local do AlugaMaquina (MongoDB + Backend Spring Boot + Frontend HTTP).
#>

$ErrorActionPreference = "Stop"
$RepoRoot = Split-Path -Parent $PSScriptRoot

# 1. Carrega .env se existir (sem sobrescrever variaveis ja definidas na sessao)
$EnvFile = Join-Path $RepoRoot ".env"
if (Test-Path $EnvFile) {
    Get-Content $EnvFile | ForEach-Object {
        if ($_ -match '^\s*([^#][^=]+)=(.*)$') {
            $name = $matches[1].Trim()
            $val = $matches[2].Trim()
            if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name, "Process"))) {
                [Environment]::SetEnvironmentVariable($name, $val, "Process")
            }
        }
    }
}

if ([string]::IsNullOrWhiteSpace($env:MONGODB_URI)) {
    $env:MONGODB_URI = "mongodb://localhost:27017/alugamaquina"
}

$usarMongoLocal = $env:MONGODB_URI -match '^mongodb://(localhost|127\.0\.0\.1)(:|/|$)'

# 2. Usa Java 21/17 (ex.: JBR do IntelliJ) se o Java padrao for mais novo que 21
$javaHome = $env:JAVA_HOME
$ideaJbr = Get-ChildItem "C:\Program Files\JetBrains\*\jbr" -Directory -ErrorAction SilentlyContinue | Select-Object -First 1
if ($ideaJbr -and (Test-Path (Join-Path $ideaJbr.FullName "bin\java.exe"))) {
    $javaHome = $ideaJbr.FullName
}

if ($usarMongoLocal) {
    Write-Host "==> [1/3] Subindo MongoDB via Docker Compose..." -ForegroundColor Cyan
    Push-Location $RepoRoot
    docker compose up -d mongodb
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        Write-Error "Falha ao iniciar o MongoDB via Docker. Abra o Docker Desktop e tente novamente."
    }
    Pop-Location

    for ($i = 0; $i -lt 30; $i++) {
        $health = docker inspect --format "{{.State.Health.Status}}" alugamaquina-mongodb 2>$null
        if ($health -eq "healthy") {
            Write-Host "    MongoDB pronto em localhost:27017/alugamaquina!" -ForegroundColor Green
            break
        }
        Start-Sleep -Seconds 1
    }
} else {
    Write-Host "==> [1/3] MONGODB_URI remota/Atlas detectada - pulando Docker local." -ForegroundColor Cyan
}

# 3. Localiza Maven
$mvnCmd = (Get-Command mvn -ErrorAction SilentlyContinue).Source
if (-not $mvnCmd) {
    $ideaMvn = Get-ChildItem "C:\Program Files\JetBrains\*\plugins\maven\lib\maven3\bin\mvn.cmd" -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($ideaMvn) {
        $mvnCmd = $ideaMvn.FullName
    }
}
if (-not $mvnCmd) {
    Write-Error "Maven nao encontrado no PATH nem no IntelliJ IDEA."
}

Write-Host "==> [2/3] Empacotando e iniciando Backend Spring Boot (porta 8080)..." -ForegroundColor Cyan
$backendDir = Join-Path $RepoRoot "backend"
$javaExe = if ($javaHome) { Join-Path $javaHome "bin\java.exe" } else { "java" }
$javaSetup = if ($javaHome) { "`$env:JAVA_HOME='$javaHome'; " } else { "" }
# O processo filho herda $env:MONGODB_URI automaticamente sem expor credenciais nos argumentos.
Start-Process -FilePath "powershell.exe" -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$backendDir'; $javaSetup& '$mvnCmd' package -DskipTests; if (`$LASTEXITCODE -eq 0) { & '$javaExe' -jar target\backend-0.1.0.jar }"
)

Write-Host "==> [3/3] Iniciando Frontend (porta 5500)..." -ForegroundColor Cyan
$pythonCmd = (Get-Command python -ErrorAction SilentlyContinue).Source
if ($pythonCmd) {
    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$RepoRoot'; & '$pythonCmd' -m http.server 5500"
    )
    $mongoExibido = $env:MONGODB_URI -replace '://[^/@]+@', '://***:***@'
    Write-Host ""
    Write-Host "Ambiente iniciado com sucesso!" -ForegroundColor Green
    Write-Host "  - API Backend : http://localhost:8080/api/status"
    Write-Host "  - Frontend    : http://localhost:5500/src/pages/login-usuario.html"
    Write-Host "  - Catalogo    : http://localhost:5500/src/pages/busca-catalogo.html"
    Write-Host "  - Chat        : http://localhost:5500/src/pages/chat.html"
    Write-Host "  - MongoDB     : $mongoExibido"
} else {
    Write-Warning "Python nao encontrado para servir o frontend. Abra src/pages/login-usuario.html via Live Server."
}
