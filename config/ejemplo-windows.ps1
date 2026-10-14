# Ejemplo de configuración local (Windows PowerShell). SIN secretos: copie a
# config/local.ps1 (ignorado por git) y complete los valores privados allí.
# No versionar config/local.ps1. Cierre la sesión o use una ventana nueva tras editar.
$env:ARQ_DB_URL = 'jdbc:mysql://127.0.0.1:33316/arquidentdb?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true'
$env:ARQ_DB_USER = 'root'
$env:ARQ_DB_PASSWORD = 'CAMBIAR-en-config-local.ps1'
# SUNAT (solo si hará consultas reales; ver docs/SUNAT.md). Sin estas variables
# la integración queda preparada y la pantalla lo indica; no se simulan respuestas.
# $env:SUNAT_CLIENT_ID = '...'
# $env:SUNAT_CLIENT_SECRET = '...'
# $env:SUNAT_RUC_CONSULTA = '...'

# Boleta automática al cobrar, con el RUC demo y modo 0 (SUNAT beta).
# Aplicar database/003_emision_demo.sql antes de activar. No requiere OAuth.
$env:FD_DEMO_ENABLED = 'true'
$env:FD_DEMO_SERIE = 'BA26'
