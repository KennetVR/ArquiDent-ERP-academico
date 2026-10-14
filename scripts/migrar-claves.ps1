# Migra las contraseñas heredadas (texto plano) a PBKDF2. Ejecutar UNA vez,
# después de 001_erp.sql y con la aplicación detenida. Requiere:
#   config/local.ps1 con ARQ_DB_URL/USER/PASSWORD (ver config/ejemplo-windows.ps1)
# No imprime ni guarda contraseñas.
. "$PSScriptRoot/../config/local.ps1"
& 'C:\Program Files\NetBeans-19\netbeans\java\maven\bin\mvn.cmd' -B -ntp -q compile
$cp = 'target/classes'
$jar = Get-ChildItem "$env:USERPROFILE/.m2/repository/com/mysql/mysql-connector-j" -Recurse -Filter 'mysql-connector-j-*.jar' |
       Sort-Object FullName -Descending | Select-Object -First 1 -ExpandProperty FullName
if (-not $jar) { throw 'No se encontró el conector MySQL en el repositorio local. Ejecute mvn package primero.' }
& java -cp "$cp;$jar" pe.edu.utp.security.MigrarClaves
