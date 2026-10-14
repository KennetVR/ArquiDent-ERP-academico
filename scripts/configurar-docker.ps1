[CmdletBinding()]
param(
    [ValidatePattern('^B[A-Z0-9]{3}$')][string]$Serie,
    [ValidateRange(1024,65535)][int]$Puerto = 8080,
    [string]$Destino = (Join-Path (Split-Path -Parent $PSScriptRoot) '.env')
)
$ErrorActionPreference = 'Stop'
if (Test-Path $Destino) { throw "Ya existe $Destino. Conserve esa configuracion o editela manualmente." }
function New-Password {
    $bytes = New-Object byte[] 24
    $rng = [Security.Cryptography.RandomNumberGenerator]::Create()
    try { $rng.GetBytes($bytes) } finally { $rng.Dispose() }
    return [Convert]::ToBase64String($bytes)
}
if (-not $Serie) {
    $alphabet = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    $Serie = 'B' + (-join (1..3 | ForEach-Object { $alphabet[(Get-Random -Maximum $alphabet.Length)] }))
}
$content = @"
MYSQL_ROOT_PASSWORD=$(New-Password)
MYSQL_PASSWORD=$(New-Password)
HTTP_PORT=$Puerto
FD_DEMO_ENABLED=true
FD_DEMO_SERIE=$Serie
"@
[IO.File]::WriteAllText($Destino, $content, [Text.UTF8Encoding]::new($false))
Write-Host "Configuracion creada: $Destino (puerto $Puerto, serie $Serie)."
Write-Host 'Siguiente paso: docker compose up -d --build --wait'
