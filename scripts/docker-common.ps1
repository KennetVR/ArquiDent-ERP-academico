function Get-DockerCommand {
    $cmd = Get-Command docker.exe -ErrorAction SilentlyContinue
    if ($cmd) { return $cmd.Source }
    $path = Join-Path $env:ProgramFiles 'Docker\Docker\resources\bin\docker.exe'
    if (Test-Path $path) { return $path }
    throw 'No se encontro Docker Desktop. Instale Docker Desktop y active contenedores Linux.'
}
function Wait-Docker([string]$Docker) {
    & $Docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { return }
    $desktop = Join-Path $env:ProgramFiles 'Docker\Docker\Docker Desktop.exe'
    if (Test-Path $desktop) { Start-Process $desktop }
    Write-Host 'Esperando a Docker Desktop...'
    $deadline = (Get-Date).AddSeconds(180)
    do {
        Start-Sleep -Seconds 3
        & $Docker info --format '{{.ServerVersion}}' 2>$null | Out-Null
        if ($LASTEXITCODE -eq 0) { return }
    } while ((Get-Date) -lt $deadline)
    throw 'Docker no esta listo. Abra Docker Desktop y revise su estado.'
}
