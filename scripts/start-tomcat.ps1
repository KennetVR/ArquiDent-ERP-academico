<#
.SYNOPSIS
  Arranca Tomcat 9 con la configuración local de base de datos (config/local.ps1).

.DESCRIPTION
  Este proyecto lee la conexión a MySQL de variables de entorno
  (ARQ_DB_URL / ARQ_DB_USER / ARQ_DB_PASSWORD) definidas en el archivo
  NO versionado config/local.ps1.

  Si Tomcat arranca sin esas variables, la aplicación responde 503
  ("Servicio no disponible. Revise la configuración privada de la base de datos").
  Este script carga config/local.ps1, resuelve JAVA_HOME y Tomcat, y arranca
  el servidor. Por defecto lo hace en segundo plano para que siga vivo aunque
  cierres la terminal.

.PARAMETER CatalinaHome
  Ruta de la instalación de Tomcat 9. Si se omite, usa $env:CATALINA_HOME y, si
  no está definida, busca en ubicaciones habituales.

.PARAMETER Build
  Ejecuta "mvn package" y copia target/arquident.war a <Tomcat>\webapps antes de
  arrancar (compilar + desplegar).

.PARAMETER Foreground
  Arranca acoplado a la consola actual (se detiene si cierras la terminal).
  Sin este modificador, Tomcat se lanza en segundo plano.

.PARAMETER Maven
  Ruta a mvn.cmd. Si se omite, lo busca en PATH y en la instalación de NetBeans 19.

.EXAMPLE
  .\scripts\start-tomcat.ps1 -Build
  Compila, despliega el WAR y arranca Tomcat en segundo plano.

.EXAMPLE
  .\scripts\start-tomcat.ps1 -Foreground
  Arranca Tomcat acoplado a la consola (Ctrl+C para detener).
#>
[CmdletBinding()]
param(
    [string]$CatalinaHome = $env:CATALINA_HOME,
    [switch]$Build,
    [switch]$Foreground,
    [string]$Maven = $env:MAVEN_CMD
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

function Write-Step($msg) { Write-Host "==> $msg" -ForegroundColor Cyan }

# --- 1) Configuración privada (credenciales de BD / SUNAT) -------------------
$localConfig = Join-Path $root 'config\local.ps1'
if (-not (Test-Path $localConfig)) {
    throw "Falta config/local.ps1. Copia config/ejemplo-windows.ps1 a config/local.ps1 y completa ARQ_DB_URL/USER/PASSWORD."
}
. $localConfig
if (-not $CatalinaHome) { $CatalinaHome = $env:CATALINA_HOME }
foreach ($v in 'ARQ_DB_URL', 'ARQ_DB_USER', 'ARQ_DB_PASSWORD') {
    if (-not (Get-Item "env:$v" -ErrorAction SilentlyContinue)) {
        throw "config/local.ps1 debe definir $v."
    }
}
Write-Step "Configuración cargada ($($env:ARQ_DB_URL))"

# --- 2) JAVA_HOME ------------------------------------------------------------
if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    $candidates = @(
        'C:\Program Files\Java\jdk-*',
        'C:\Program Files\Eclipse Adoptium\jdk-*',
        'C:\Program Files\Microsoft\jdk-*'
    )
    foreach ($c in $candidates) {
        $hit = Get-Item $c -ErrorAction SilentlyContinue |
               Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
               Sort-Object Name -Descending | Select-Object -First 1
        if ($hit) { $env:JAVA_HOME = $hit.FullName; break }
    }
}
if (-not $env:JAVA_HOME) { throw 'No se encontró JAVA_HOME. Defina la variable JAVA_HOME.' }
Write-Step "JAVA_HOME = $env:JAVA_HOME"

# --- 3) Tomcat 9 -------------------------------------------------------------
if (-not $CatalinaHome) {
    $patterns = @(
        (Join-Path $root '.runtime\tomcat9'),
        'C:\Program Files\Apache Software Foundation\Tomcat 9.0*',
        'C:\apache-tomcat-9*',
        "$env:LOCALAPPDATA\Temp\opencode\tomcat9\apache-tomcat-9*"
    )
    foreach ($p in $patterns) {
        $hit = Get-Item $p -ErrorAction SilentlyContinue |
               Where-Object { Test-Path (Join-Path $_.FullName 'bin\catalina.bat') } |
               Select-Object -First 1
        if ($hit) { $CatalinaHome = $hit.FullName; break }
    }
}
if (-not $CatalinaHome -or -not (Test-Path (Join-Path $CatalinaHome 'bin\catalina.bat'))) {
    throw 'No se encontró Tomcat 9. Use -CatalinaHome <ruta> o defina CATALINA_HOME.'
}
$env:CATALINA_HOME = $CatalinaHome
$env:CATALINA_BASE = $CatalinaHome
$catalina = Join-Path $CatalinaHome 'bin\catalina.bat'
Write-Step "Tomcat = $CatalinaHome"

# --- 4) Comprobar puerto antes de modificar el despliegue --------------------
function Get-Port8080Pid {
    try {
        $c = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction Stop
        return [int]($c | Select-Object -First 1).OwningProcess
    } catch {
        $line = netstat -ano | Select-String '^\s*TCP\s+\S+:8080\s+\S+\s+LISTENING\s+[1-9][0-9]*\s*$' | Select-Object -First 1
        if ($line) { return [int](($line.Line.Trim() -split '\s+')[-1]) }
        return $null
    }
}
$busy = Get-Port8080Pid
if ($busy) {
    if ($Build) { throw "Detenga Tomcat con scripts/stop-tomcat.ps1 antes de usar -Build (PID $busy)." }
    Write-Host "El puerto 8080 ya esta activo (PID $busy)."
    return
}

# --- 5) Compilar y desplegar (opcional) --------------------------------------
if ($Build) {
    if (-not $Maven) {
        $cmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
        if ($cmd) { $Maven = $cmd.Source }
        elseif (Test-Path 'C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd') {
            $Maven = 'C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd'
        }
        else { throw 'No se encontró Maven. Use -Maven <ruta a mvn.cmd>.' }
    }
    Write-Step "Compilando (mvn package)..."
    Push-Location $root
    try {
        & $Maven -B -ntp -DskipTests package
        if ($LASTEXITCODE -ne 0) { throw "mvn package falló (código $LASTEXITCODE)." }
    } finally { Pop-Location }
    $war = Join-Path $root 'target\arquident.war'
    Copy-Item $war (Join-Path $CatalinaHome 'webapps\arquident.war') -Force
    Write-Step "WAR desplegado en $CatalinaHome\webapps\arquident.war"
}

# --- 6) Arrancar -------------------------------------------------------------
Write-Step 'Iniciando Tomcat...'
if ($Foreground) {
    & $catalina run
    return
}

Start-Process -FilePath 'cmd.exe' `
    -ArgumentList '/c', ('"{0}" run' -f $catalina) `
    -WorkingDirectory (Join-Path $CatalinaHome 'bin') `
    -WindowStyle Hidden | Out-Null

$deadline = (Get-Date).AddSeconds(90)
do {
    try {
        $r = Invoke-WebRequest -Uri 'http://127.0.0.1:8080/arquident/servicios' -UseBasicParsing -TimeoutSec 5
        if ($r.StatusCode -eq 200) {
            Write-Host 'Tomcat y base de datos listos.' -ForegroundColor Green
            Write-Host 'App:   http://127.0.0.1:8080/arquident/inicio'
            Write-Host "Logs:  $CatalinaHome\logs"
            return
        }
    } catch { }
    Start-Sleep -Seconds 2
} while ((Get-Date) -lt $deadline)
throw "La aplicacion no estuvo lista en 90s. Revise MySQL, config/local.ps1 y $CatalinaHome\logs."
