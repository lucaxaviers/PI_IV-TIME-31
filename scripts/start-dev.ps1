<#
.SYNOPSIS
    Inicializa com um unico comando todo o ambiente do AlugaMaquina:
    Docker/MongoDB (ou MongoDB Atlas) + Backend Spring Boot (8080) + Servidor TCP POO (3000) + Cliente TCP + Frontend (5500).
#>

[CmdletBinding()]
param(
    [switch]$SemClienteTcp
)

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
    Write-Host "==> [1/5] Verificando Docker e subindo MongoDB via Docker Compose..." -ForegroundColor Cyan

    $dockerDesktopExe = "C:\Program Files\Docker\Docker\Docker Desktop.exe"
    $dockerOk = $false
    docker info *> $null
    if ($LASTEXITCODE -eq 0) {
        $dockerOk = $true
    } elseif (Test-Path $dockerDesktopExe) {
        Write-Host "    Docker parado. Iniciando Docker Desktop automaticamente..." -ForegroundColor Yellow
        Start-Process -FilePath $dockerDesktopExe
        for ($i = 0; $i -lt 45; $i++) {
            Start-Sleep -Seconds 1
            docker info *> $null
            if ($LASTEXITCODE -eq 0) {
                $dockerOk = $true
                Write-Host "    Docker Desktop pronto!" -ForegroundColor Green
                break
            }
        }
    }

    if (-not $dockerOk) {
        Write-Error "Docker nao respondeu em 45s. Aguarde o Docker Desktop terminar de abrir e execute .\scripts\start-dev.ps1 novamente."
    }

    Push-Location $RepoRoot
    docker compose up -d mongodb
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        Write-Error "Falha ao iniciar o MongoDB via Docker Compose."
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
    Write-Host "==> [1/5] MONGODB_URI do MongoDB Atlas/remoto detectada - conectando direto na nuvem (Docker local nao necessario)." -ForegroundColor Cyan
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

Write-Host "==> [2/5] Empacotando e iniciando API Backend Spring Boot (porta 8080)..." -ForegroundColor Cyan
$backendDir = Join-Path $RepoRoot "backend"
$javaExe = if ($javaHome) { Join-Path $javaHome "bin\java.exe" } else { "java" }
$javaSetup = if ($javaHome) { "`$env:JAVA_HOME='$javaHome'; " } else { "" }
# O processo filho herda $env:MONGODB_URI automaticamente sem expor credenciais nos argumentos.
Start-Process -FilePath "powershell.exe" -ArgumentList @(
    "-NoExit",
    "-Command",
    "Set-Location '$backendDir'; $javaSetup& '$mvnCmd' package -DskipTests; if (`$LASTEXITCODE -eq 0) { & '$javaExe' -jar target\backend-0.1.0.jar }"
)

# 4. Localiza javac/java compatíveis para o Servidor e Cliente TCP POO
$pooJavacExe = (Get-Command javac -ErrorAction SilentlyContinue).Source
if (-not $pooJavacExe -and $javaHome -and (Test-Path (Join-Path $javaHome "bin\javac.exe"))) {
    $pooJavacExe = Join-Path $javaHome "bin\javac.exe"
}
$pooJavaExe = if ($pooJavacExe) { Join-Path (Split-Path -Parent $pooJavacExe) "java.exe" } else { $javaExe }

$servidorPooDir = Join-Path $RepoRoot "servidor-poo\Servidor"
$clientePooDir  = Join-Path $RepoRoot "servidor-poo\Cliente"

if ($pooJavacExe -and (Test-Path $servidorPooDir)) {
    Write-Host "==> [3/5] Compilando e iniciando Servidor Java TCP POO (porta 3000)..." -ForegroundColor Cyan
    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$servidorPooDir'; & '$pooJavacExe' -encoding UTF-8 *.java; if (`$LASTEXITCODE -eq 0) { & '$pooJavaExe' Servidor }"
    )

    if (-not $SemClienteTcp -and (Test-Path $clientePooDir)) {
        Write-Host "==> [4/5] Compilando e iniciando Cliente Java TCP POO..." -ForegroundColor Cyan
        Start-Process -FilePath "powershell.exe" -ArgumentList @(
            "-NoExit",
            "-Command",
            "Set-Location '$clientePooDir'; & '$pooJavacExe' -encoding UTF-8 *.java; if (`$LASTEXITCODE -eq 0) { Start-Sleep -Seconds 2; & '$pooJavaExe' Cliente }"
        )
    } else {
        Write-Host "==> [4/5] Cliente Java TCP POO pulado (-SemClienteTcp)." -ForegroundColor DarkGray
    }
} else {
    Write-Warning "javac nao encontrado ou pasta servidor-poo\Servidor ausente - pulando Servidor TCP POO."
}

Write-Host "==> [5/5] Iniciando Frontend Web (porta 5500)..." -ForegroundColor Cyan
$pythonCmd = (Get-Command python -ErrorAction SilentlyContinue).Source
$mongoExibido = $env:MONGODB_URI -replace '://[^/@]+@', '://***:***@'
if ($pythonCmd) {
    Start-Process -FilePath "powershell.exe" -ArgumentList @(
        "-NoExit",
        "-Command",
        "Set-Location '$RepoRoot'; & '$pythonCmd' -m http.server 5500"
    )
    Write-Host ""
    Write-Host "Ambiente completo iniciado com sucesso!" -ForegroundColor Green
    Write-Host "  - API Spring Boot : http://localhost:8080/api/status"
    Write-Host "  - Servidor TCP POO: localhost:3000 (servidor-poo\Servidor)"
    Write-Host "  - Frontend Web    : http://localhost:5500/src/pages/login-usuario.html"
    Write-Host "  - Catalogo        : http://localhost:5500/src/pages/busca-catalogo.html"
    Write-Host "  - Chat            : http://localhost:5500/src/pages/chat.html"
    Write-Host "  - MongoDB         : $mongoExibido"
} else {
    Write-Warning "Python nao encontrado para servir o frontend. Abra src/pages/login-usuario.html via Live Server."
}
