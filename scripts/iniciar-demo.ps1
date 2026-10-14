[CmdletBinding()]
param(
    [string]$DatabaseContainer = 'arquident-academico-mysql',
    [string]$CatalinaHome = $env:CATALINA_HOME,
    [switch]$SinNavegador
)
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
if (-not (Test-Path (Join-Path $root 'config/local.ps1'))) {
    throw 'Falta config/local.ps1. Para una PC nueva siga docs/DESPLIEGUE.md (Docker).'
}
. (Join-Path $PSScriptRoot 'docker-common.ps1')
$docker = Get-DockerCommand
Wait-Docker $docker
$running = & $docker inspect --format '{{.State.Running}}' $DatabaseContainer 2>$null
if ($LASTEXITCODE -ne 0) { throw "No existe la base $DatabaseContainer. Consulte docs/DESPLIEGUE.md." }
if ($running -ne 'true') {
    & $docker start $DatabaseContainer | Out-Null
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo iniciar MySQL.' }
}
$ready = $false
for ($i=0; $i -lt 60; $i++) {
    & $docker exec $DatabaseContainer mysqladmin ping --silent 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ready=$true; break }
    Start-Sleep -Seconds 2
}
if (-not $ready) { throw 'MySQL no responde. Revise Docker Desktop.' }
& (Join-Path $PSScriptRoot 'start-tomcat.ps1') -CatalinaHome $CatalinaHome
& (Join-Path $PSScriptRoot 'verificar-demo.ps1') -ConUsuariosDemo
if (-not $SinNavegador) { Start-Process 'http://127.0.0.1:8080/arquident/login' }
Write-Host 'ArquiDent listo para la presentacion.' -ForegroundColor Green
