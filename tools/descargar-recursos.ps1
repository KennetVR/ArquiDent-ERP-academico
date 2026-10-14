# Recursos de interfaz empaquetados en el WAR. Ejecutar solo para regenerarlos.
$ErrorActionPreference = 'Stop'
$assets = Join-Path (Split-Path -Parent $PSScriptRoot) 'src/main/webapp/assets'
$fa = 'https://cdn.jsdelivr.net/npm/@fortawesome/fontawesome-free@7.1.0'
$poppins = 'https://cdn.jsdelivr.net/npm/@fontsource/poppins@5.3.0'
$downloads = @{
    'fontawesome/css/all.min.css' = "$fa/css/all.min.css"
    'fontawesome/webfonts/fa-solid-900.woff2' = "$fa/webfonts/fa-solid-900.woff2"
    'fontawesome/webfonts/fa-regular-400.woff2' = "$fa/webfonts/fa-regular-400.woff2"
    'fontawesome/webfonts/fa-brands-400.woff2' = "$fa/webfonts/fa-brands-400.woff2"
    'fontawesome/LICENSE.txt' = "$fa/LICENSE.txt"
    'poppins/LICENSE' = "$poppins/LICENSE"
}
foreach ($weight in 300,400,500,600,700) {
    $downloads["poppins/poppins-latin-$weight-normal.woff2"] = "$poppins/files/poppins-latin-$weight-normal.woff2"
}
foreach ($file in $downloads.Keys) {
    $destination = Join-Path $assets $file
    [void][IO.Directory]::CreateDirectory((Split-Path -Parent $destination))
    Invoke-WebRequest -UseBasicParsing -Uri $downloads[$file] -OutFile $destination
    Write-Host "Descargado: $file"
}
