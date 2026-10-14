# Instalación y despliegue

## 1. Equipo actual: arranque diario

Abrir Docker Desktop y ejecutar **`INICIAR-DEMO.cmd`** desde la carpeta del proyecto.
El lanzador usa el contenedor `arquident-academico-mysql`, carga `config/local.ps1`,
arranca Tomcat y comprueba páginas, base de datos y cuentas demo.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/iniciar-demo.ps1
```

URL: <http://127.0.0.1:8080/arquident/login>.
La instalación preparada conserva Tomcat en `.runtime/tomcat9` y MySQL en su volumen
de Docker. La configuración local y estos datos se mantienen en el equipo.

Para detener Tomcat:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/stop-tomcat.ps1
```

## 2. Instalación nueva con Docker (Windows, Linux o macOS)

### Requisitos

- Docker Engine + Compose v2, o Docker Desktop con contenedores Linux.
- Git, o descargar y extraer el ZIP del repositorio.
- Puerto local 8080 disponible. La primera construcción necesita Internet.

```powershell
git clone https://github.com/KennetVR/ArquiDent-ERP-academico.git
cd ArquiDent-ERP-academico
powershell -ExecutionPolicy Bypass -File scripts/configurar-docker.ps1
docker compose up -d --build --wait
docker compose ps
```

`db` y `web` deben indicar **healthy**. Abrir
<http://127.0.0.1:8080/arquident/inicio> y usar las cuentas del README.

El configurador genera claves privadas y una serie `Bxxx`. Conserva `.env` junto
con el volumen de MySQL: los correlativos se guardan en esa base. El RUC beta es
compartido con el ejemplo del proveedor; una base nueva debe utilizar una serie
propia para evitar reutilizar documentos de otra instalación. La serie generada
reduce coincidencias, pero no reserva globalmente una serie en el proveedor.

**Sin PowerShell:** `cp .env.example .env`, editar los valores y ejecutar Compose.
Para otro puerto, definir `HTTP_PORT=8081` en `.env`; la URL pasará a usar `8081`.
También se puede generar con `scripts/configurar-docker.ps1 -Puerto 8081`.

### Qué se instala

- Maven y JDK 21 compilan el WAR en una etapa de construcción.
- Tomcat 9 / JDK 21 ejecutan `/arquident`.
- MySQL 8.4 ejecuta las cuatro migraciones en orden **solo al crear un volumen vacío**.
- El volumen `mysql-data` conserva usuarios, citas, pagos y correlativos.
- La aplicación se publica en `127.0.0.1`; MySQL queda en la red interna de Compose.

### Uso posterior

```powershell
docker compose up -d --wait       # arrancar lo ya construido
docker compose stop              # detener conservando los datos
docker compose logs --tail 100 web
docker compose logs --tail 100 db
```

`docker compose down` conserva el volumen. **No usar `down -v` para el arranque
diario:** elimina los datos. Una actualización de código se aplica con
`git pull` y `docker compose up -d --build --wait`; si añade migraciones, revisar
sus instrucciones porque el directorio de inicialización no se reejecuta.

## 3. Instalación manual: Java + Tomcat + MySQL

### Requisitos

- **JDK 21 recomendado**; el código se compila para Java 11.
- Maven 3.9 o Maven de NetBeans.
- **Tomcat 9**. Tomcat 10/11 usa Jakarta y no admite directamente estos Servlets `javax`.
- MySQL 8.0.16+ o 8.4.

### Base nueva

Con MySQL en marcha, ejecutar desde la raíz del proyecto. Estos comandos también
funcionan en PowerShell (usan `source` de MySQL, no redirección `<`).

```powershell
mysql -u root -p -e "CREATE DATABASE arquidentdb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql --default-character-set=utf8mb4 -u root -p arquidentdb -e "source database/000_base_nueva.sql; source database/001_erp.sql; source database/002_demo.sql; source database/003_emision_demo.sql;"
```

Para un MySQL en otro puerto, añadir `-h 127.0.0.1 -P 33316` según corresponda.
`000_base_nueva.sql` es exclusivamente para una base vacía. Una base heredada necesita
respaldo y revisión de `001_erp.sql`; la migración con datos antiguos no fue validada.

### Variables privadas

```powershell
Copy-Item config/ejemplo-windows.ps1 config/local.ps1
notepad config/local.ps1
```

Completar `ARQ_DB_URL`, `ARQ_DB_USER`, `ARQ_DB_PASSWORD`. Si MySQL es local en 3306,
ajustar la URL del ejemplo (el equipo preparado utiliza 33316).

| Variable | Uso |
|---|---|
| `ARQ_DB_URL` | `jdbc:mysql://127.0.0.1:3306/arquidentdb?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true` |
| `ARQ_DB_USER`, `ARQ_DB_PASSWORD` | Usuario y clave de la base local. |
| `FD_DEMO_ENABLED` | `true` para boletas beta al cobrar; `false` para registrar solo el cobro. |
| `FD_DEMO_SERIE` | Serie de cuatro caracteres: `B` + tres letras mayúsculas/dígitos. |
| `SUNAT_CLIENT_ID`, `SUNAT_CLIENT_SECRET`, `SUNAT_RUC_CONSULTA` | Solo para consulta oficial OAuth; no son necesarios para emitir en beta. |
| `JAVA_HOME`, `CATALINA_HOME`, `MAVEN_CMD` | Rutas de las herramientas cuando no son detectables. |

`config/local.ps1` configura Tomcat nativo; `.env` configura Docker Compose.
Ambos archivos son locales y están excluidos de Git.

### Compilar y desplegar

Ejemplo de ubicaciones (ajustar a las instalaciones reales):

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-21'
$env:CATALINA_HOME = 'C:\apps\tomcat9'
mvn -B -ntp verify
powershell -ExecutionPolicy Bypass -File scripts/start-tomcat.ps1 -Build
```

`-Build` empaqueta y copia `target/arquident.war` a `webapps/`. Omite las pruebas
en ese paso porque `mvn verify` se ejecuta por separado. Detener primero una
instancia activa con `scripts/stop-tomcat.ps1` antes de actualizar el WAR.
El arranque carga la configuración y espera a que `/servicios` confirme conexión MySQL.

También puede descargarse `arquident.war` de **Releases**, copiarlo a
`<Tomcat>/webapps/arquident.war` y ejecutar `scripts/start-tomcat.ps1` sin `-Build`.
El WAR incluye librerías y recursos de interfaz; requiere Tomcat y la base configurada.

### Verificación

```powershell
powershell -ExecutionPolicy Bypass -File scripts/verificar-demo.ps1 -ConUsuariosDemo
# Si usa otro puerto:
powershell -ExecutionPolicy Bypass -File scripts/verificar-demo.ps1 -BaseUrl http://127.0.0.1:8081/arquident -ConUsuariosDemo
```

Comprueba páginas, recursos visuales, acceso de administrador/paciente, navegación
y Caja. No reserva ni cobra. Omitir `-ConUsuariosDemo` si se cambiaron esas cuentas.

## 4. Respaldar y restaurar

Equipo preparado:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/backup-db.ps1
```

Para Compose, obtener el identificador y pasarlo al mismo script:

```powershell
$db = docker compose ps -q db
powershell -ExecutionPolicy Bypass -File scripts/backup-db.ps1 -Container $db
```

Se genera `backups/arquidentdb-<fecha>-<id>.sql`, sin alteración de datos. Guardar
una copia del SQL y de la configuración privada en el equipo/USB de presentación.
`backups/` se excluye del repositorio público.

Para restaurar **en una base vacía de recuperación**, copiar el SQL al contenedor
y ejecutarlo con MySQL. El dump incluye las tablas; no ejecutar primero las cuatro
migraciones en esa base. Ejemplo con el contenedor local:

```powershell
docker cp backups/ARCHIVO.sql arquident-academico-mysql:/tmp/arquident-respaldo.sql
docker exec -it arquident-academico-mysql mysql -uroot -p -e "CREATE DATABASE arquident_recuperada CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
docker exec -it arquident-academico-mysql mysql -uroot -p arquident_recuperada -e "source /tmp/arquident-respaldo.sql;"
```

Apuntar `ARQ_DB_URL` a `arquident_recuperada` y reiniciar Tomcat. Conservar la serie
de la base restaurada. Para Compose, cambiar `ARQ_DB_URL` en `compose.yaml` y recrear
`web` si se desea usar el esquema recuperado.

## 5. Pruebas con MySQL aislado

Crear `arquident_cpe_test` y aplicar las cuatro migraciones en él. Nunca utilizar
la base de presentación para estas pruebas:

```powershell
$env:ARQ_TEST_DB_URL = 'jdbc:mysql://127.0.0.1:33316/arquident_cpe_test?serverTimezone=America/Lima&useSSL=false&allowPublicKeyRetrieval=true'
$env:ARQ_TEST_DB_USER = 'root'
$env:ARQ_TEST_DB_PASSWORD = 'CLAVE_LOCAL'
mvn -B -ntp verify
```

Resultado esperado: **33 pruebas, 0 fallos, 0 errores, 0 omitidas**. Sin esas
variables se ejecutan 28 y se omiten 5. GitHub Actions automatiza la base aislada.

## 6. Solución de problemas

| Síntoma | Acción |
|---|---|
| Docker no responde | Abrir Docker Desktop y esperar a que el motor Linux esté listo. |
| Puerto 8080 ocupado | Usar la instalación ya activa, detener su Tomcat, o elegir `HTTP_PORT=8081` en Compose. |
| HTTP 503 | Comprobar MySQL, `ARQ_DB_*` y migraciones; reiniciar Tomcat después de cambiar configuración. |
| HTTP 404 en `/arquident` | Comprobar que el archivo se llama `arquident.war`, usar Tomcat 9 y revisar logs. |
| HTTP 403 al enviar formulario | Recargar para renovar CSRF e iniciar sesión con el rol apropiado. |
| No hay horarios | Seleccionar un servicio asignado al odontólogo y una fecha con jornada configurada. |
| No aparecen pendientes | Reservar una cita y ampliar el rango de fechas de Caja. |
| Boleta por revisar | Abrir Detalle SUNAT. El pago permanece cobrado; revisar el mismo documento, sin cobrar otra vez. |
| PDF no disponible | Revisar Internet y el proveedor. PDF/XML/CDR se descargan del servicio externo. |
| Cambié una JSP o clase | Volver a compilar/desplegar; editar `src` no actualiza automáticamente un WAR instalado. |

Logs nativos: `<Tomcat>/logs/`. Logs Docker: `docker compose logs --tail 100 web db`.
