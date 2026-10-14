[CmdletBinding()]
param(
    [string]$Container = 'arquident-academico-mysql',
    [ValidatePattern('^[A-Za-z0-9_]+$')][string]$Database = 'arquidentdb'
)
$ErrorActionPreference = 'Stop'
. (Join-Path $PSScriptRoot 'docker-common.ps1')
$docker = Get-DockerCommand
$dir = Join-Path (Split-Path -Parent $PSScriptRoot) 'backups'
[void][IO.Directory]::CreateDirectory($dir)
$name = "$Database-$(Get-Date -Format 'yyyyMMdd-HHmmss')-$([guid]::NewGuid().ToString('N').Substring(0,6)).sql"
$remote = "/tmp/$name"
try {
    $command = 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -uroot --single-transaction --routines --triggers --events --no-tablespaces --set-gtid-purged=OFF --result-file=' + $remote + ' ' + $Database
    & $docker exec $Container sh -c $command
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo generar el respaldo.' }
    & $docker cp "${Container}:$remote" (Join-Path $dir $name)
    if ($LASTEXITCODE -ne 0) { throw 'No se pudo copiar el respaldo.' }
    Write-Host "Respaldo guardado en backups/$name" -ForegroundColor Green
} finally {
    & $docker exec $Container rm -f $remote | Out-Null
}
