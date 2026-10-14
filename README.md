<p align="center"><img src="src/main/webapp/images/arquident-logo.png" alt="ArquiDent — Clínica dental" width="240"></p>

# ArquiDent · ERP odontológico académico

[![Compilar y probar](https://github.com/KennetVR/ArquiDent-ERP-academico/actions/workflows/ci.yml/badge.svg)](https://github.com/KennetVR/ArquiDent-ERP-academico/actions/workflows/ci.yml)

Aplicación Java con **Servlets, JSP, Maven, MySQL y arquitectura MVC–DAO**.
Gestiona pacientes, servicios, citas, atención y caja. Al cobrar puede emitir una
boleta de prueba mediante **Facturación Directa → SUNAT beta**, con PDF ArquiDent,
XML y CDR.

## Para la presentación

**En el equipo ya configurado:** abrir Docker Desktop y hacer doble clic en
[`INICIAR-DEMO.cmd`](INICIAR-DEMO.cmd). Arranca MySQL/Tomcat, verifica el acceso
y abre <http://127.0.0.1:8080/arquident/login>.

**[Guía de presentación](docs/PRESENTACION.md)** ·
**[Instalación y despliegue](docs/DESPLIEGUE.md)** ·
**[Descargar versión preparada](https://github.com/KennetVR/ArquiDent-ERP-academico/releases)**

## Instalar en otra PC con Docker

Requisitos: Docker Desktop con contenedores Linux y Git. La primera instalación
descarga las imágenes y dependencias; puede tardar varios minutos.

```powershell
git clone https://github.com/KennetVR/ArquiDent-ERP-academico.git
cd ArquiDent-ERP-academico
powershell -ExecutionPolicy Bypass -File scripts/configurar-docker.ps1
docker compose up -d --build --wait
```

Abrir <http://127.0.0.1:8080/arquident/login>. Compose crea una base propia con
usuarios, servicios y horarios de demostración. Las claves de MySQL y la serie
de boletas se generan en `.env`, que se conserva en tu equipo.

En Linux/macOS: copiar `.env.example` a `.env`, completar las dos claves y una
serie propia (`B` + tres letras/dígitos), y ejecutar el mismo comando de Compose.

Para arrancar nuevamente: `docker compose up -d --wait`.
Para detener conservando los datos: `docker compose stop`.

La guía incluye instalación manual con **JDK 21, Maven y Tomcat 9**, configuración,
respaldos y solución de problemas. **Tomcat 10/11 no es compatible** con esta aplicación
basada en `javax.servlet`.

## Cuentas de demostración

| Rol | Correo | Contraseña |
|---|---|---|
| Administrador | `admin.demo@arquident.edu` | `AdminDemo-2026-01` |
| Secretaría / Caja | `caja.demo@arquident.edu` | `CajaDemo-2026-02` |
| Odontólogo | `odonto1.demo@arquident.edu` | `OdontoDemo-2026-03` |
| Odontólogo (martes/jueves) | `odonto2.demo@arquident.edu` | `OdontoDemo-2026-04` |
| Paciente | `paci1.demo@arquident.edu` | `PacienteDemo-2026-05` |

Son cuentas ficticias de [`database/002_demo.sql`](database/002_demo.sql).

## Funciones

| Módulo | Funciones principales |
|---|---|
| Pacientes | Registro, validación, actualización e historial según permisos. Pestaña oculta al paciente. |
| Recursos | Servicios, precios, duración, odontólogos, horarios y asignaciones. Solo administrador. |
| Citas / Atención | Disponibilidad real, reservas sin cruces, cancelación, observaciones, receta y cierre. |
| Caja | Cobros, ingresos por fechas, boletas automáticas, PDF con logo, XML/CDR y consulta externa. |

**Flujo:** reserva → pago pendiente → cobro → cita confirmada → atención → historial.
Autenticación por sesión, contraseñas PBKDF2, permisos por rol, CSRF y transacciones SQL.

La interfaz incluye Poppins, iconos y estilos dentro del WAR; funciona sin CDN.
La emisión de boletas y descarga del PDF/XML/CDR del proveedor requieren Internet.
La emisión beta es académica; la consulta oficial OAuth se configura por separado.

## Compilar y verificar

```powershell
mvn -B -ntp verify
# Resultado: target/arquident.war
powershell -ExecutionPolicy Bypass -File scripts/verificar-demo.ps1 -ConUsuariosDemo
```

Suite: **28 pruebas autónomas + 5 de integración MySQL**. Las cinco de MySQL se omiten
si no se configura `ARQ_TEST_DB_*`; GitHub Actions prepara una base aislada y ejecuta
las **33**. Las pruebas automatizadas no envían boletas al proveedor.

## Documentación

- [Despliegue, configuración y solución de problemas](docs/DESPLIEGUE.md)
- [Preparación y recorrido para exponer](docs/PRESENTACION.md)
- [Guion de los cuatro módulos](docs/DEMO.md)
- [Arquitectura y permisos](docs/ARQUITECTURA.md)
- [Integración SUNAT](docs/SUNAT.md) y [prueba paso a paso de Caja](docs/PRUEBA_CAJA_SUNAT.md)
- [Calidad y resultados de pruebas](docs/CALIDAD.md)
- [Dependencias y recursos visuales](docs/DEPENDENCIAS.md)
- [Historial de versiones](CHANGELOG.md) y [guía de contribución](CONTRIBUTING.md)

## Estructura

```text
src/main/java/pe/edu/utp/  controladores, servicios, DAO, seguridad, SUNAT y PDF
src/main/webapp/          JSP, estilos, fuentes, iconos y logos
src/test/                pruebas JUnit
database/                esquema y migraciones 000 → 001 → 002 → 003
config/                  ejemplo para Tomcat nativo
scripts/                 arranque, verificación y respaldo
docs/                    guías técnicas y presentación
tools/                   regeneración opcional de estilos (Node no es necesario para ejecutar)
```
