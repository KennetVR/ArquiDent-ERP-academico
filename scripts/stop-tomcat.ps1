<#
.SYNOPSIS
  Detiene la instancia de Tomcat 9 del proyecto ArquiDent.

.DESCRIPTION
  Intenta un apagado ordenado con bin\shutdown.bat. Si el puerto 8080 sigue
  escuchando, fuerza la detención del proceso que lo ocupa.

.PARAMETER CatalinaHome
  Ruta de la instalación de Tomcat 9. Igual que en start-tomcat.ps1.

.EXAMPLE
  .\scripts\stop-tomcat.ps1
#>
[CmdletBinding()]
param(
    [string]$CatalinaHome = $env:CATALINA_HOME
)

$ErrorActionPreference = 'Stop'

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

if (-not $env:JAVA_HOME -or -not (Test-Path (Join-Path $env:JAVA_HOME 'bin\java.exe'))) {
    foreach ($c in @('C:\Program Files\Java\jdk-*', 'C:\Program Files\Eclipse Adoptium\jdk-*', 'C:\Program Files\Microsoft\jdk-*')) {
        $hit = Get-Item $c -ErrorAction SilentlyContinue |
               Where-Object { Test-Path (Join-Path $_.FullName 'bin\java.exe') } |
               Sort-Object Name -Descending | Select-Object -First 1
        if ($hit) { $env:JAVA_HOME = $hit.FullName; break }
    }
}

if (-not $CatalinaHome) {
    $patterns = @(
        (Join-Path (Split-Path -Parent $PSScriptRoot) '.runtime\tomcat9'),
        'C:\Program Files\Apache Software Foundation\Tomcat 9.0*',
        'C:\apache-tomcat-9*',
        "$env:LOCALAPPDATA\Temp\opencode\tomcat9\apache-tomcat-9*"
    )
    foreach ($p in $patterns) {
        $hit = Get-Item $p -ErrorAction SilentlyContinue |
               Where-Object { Test-Path (Join-Path $_.FullName 'bin\shutdown.bat') } |
               Select-Object -First 1
        if ($hit) { $CatalinaHome = $hit.FullName; break }
    }
}

$pid8080 = Get-Port8080Pid
if (-not $pid8080) {
    Write-Host 'Tomcat no está escuchando en 8080 (ya está detenido).'
    return
}

if ($CatalinaHome -and (Test-Path (Join-Path $CatalinaHome 'bin\shutdown.bat'))) {
    $env:CATALINA_HOME = $CatalinaHome
    $env:CATALINA_BASE = $CatalinaHome
    Write-Host "==> Apagado ordenado desde $CatalinaHome ..." -ForegroundColor Cyan
    & (Join-Path $CatalinaHome 'bin\shutdown.bat') | Out-Null
    $deadline = (Get-Date).AddSeconds(20)
    while ((Get-Date) -lt $deadline -and (Get-Port8080Pid)) { Start-Sleep -Seconds 1 }
}

if (Get-Port8080Pid) {
    $p = Get-Port8080Pid
    $process = Get-CimInstance Win32_Process -Filter "ProcessId=$p"
    if (-not $process -or $process.Name -ne 'java.exe' -or $process.CommandLine -notlike '*org.apache.catalina.startup.Bootstrap*' -or $process.CommandLine -notlike "*$CatalinaHome*") {
        throw "El proceso $p no es la instancia de Tomcat seleccionada."
    }
    Write-Warning "El apagado ordenado no cerró el puerto; deteniendo proceso PID $p."
    Stop-Process -Id $p -Force
    Start-Sleep -Seconds 2
    if (Get-Port8080Pid) { throw 'No se pudo detener Tomcat.' }
}

Write-Host 'Tomcat detenido.' -ForegroundColor Green
