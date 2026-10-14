[CmdletBinding()]
param(
    [string]$BaseUrl = 'http://127.0.0.1:8080/arquident',
    [switch]$ConUsuariosDemo
)
$ErrorActionPreference = 'Stop'
$BaseUrl = $BaseUrl.TrimEnd('/')
foreach ($path in '/inicio','/servicios','/login','/css/tailwind.css','/css/erp.css','/images/arquident-logo.png','/assets/fontawesome/webfonts/fa-solid-900.woff2','/assets/poppins/poppins-latin-400-normal.woff2') {
    $r = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl$path" -TimeoutSec 20
    if ($r.StatusCode -ne 200) { throw "Fallo en $path : HTTP $($r.StatusCode)" }
    Write-Host "OK $path"
}
if ($ConUsuariosDemo) {
    foreach ($account in @(
        @{correo='admin.demo@arquident.edu'; clave='AdminDemo-2026-01'; pacientes=$true},
        @{correo='paci1.demo@arquident.edu'; clave='PacienteDemo-2026-05'; pacientes=$false}
    )) {
        $session = New-Object Microsoft.PowerShell.Commands.WebRequestSession
        $login = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/login" -WebSession $session
        $token = [regex]::Match($login.Content, 'name="csrf" value="([^"]+)"').Groups[1].Value
        if (-not $token) { throw 'No se encontro el formulario de acceso.' }
        $r = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/login" -Method Post -WebSession $session -Body @{
            correo=$account.correo; contrasena=$account.clave; csrf=$token
        }
        if ($r.Content -notmatch 'action="[^"]*/logout"') { throw "No pudo ingresar $($account.correo)." }
        $nav = [regex]::Match($r.Content, '(?s)<div class="navlinks".*?</div>').Value
        if (($nav -match 'href="[^"]*/pacientes"') -ne $account.pacientes) { throw 'Navegacion por rol incorrecta.' }
        if ($account.pacientes) {
            $caja = Invoke-WebRequest -UseBasicParsing -Uri "$BaseUrl/caja" -WebSession $session
            if ($caja.Content -notmatch 'Caja y comprobantes') { throw 'Caja no esta disponible.' }
        }
        Write-Host "OK acceso y permisos: $($account.correo)"
    }
}
Write-Host 'Verificacion completada (sin reservar citas ni registrar cobros).' -ForegroundColor Green
