# ArquiDent — ERP odontológico académico (alcance reducido)

Prototipo sobre el proyecto ArquiDent original. Misma tecnología
(Java 11, Servlets + JSP, Maven, MySQL, MVC con DAO) y mismo diseño Tailwind.
Sin microservicios ni cambios de framework.

## Los cuatro módulos (rutas)

- **A. Pacientes** `/pacientes` — registro/validación/actualización + historial de
  citas, atenciones y pagos. Cada rol ve solo lo autorizado.
- **B. Recursos** `/recursos` (administrador) — odontólogos, horarios laborales,
  servicios/precios y asignación servicio↔odontólogo. Las reservas usan estos datos.
- **C. Citas** `/citas` — reservar/cancelar, sin cruces ni fuera de horario, inicio/fin
  de atención con observaciones, receta y derivación, transiciones válidas.
- **D. Caja** `/caja` (secretaría/administrador) — pago pendiente por cita, cobro con
  método/fecha (confirma la cita en la misma transacción), pendientes e ingresos por
  fechas y **boleta demo automática al cobrar** (SUNAT beta vía Facturación Directa,
  con PDF/XML/CDR). También permite registrar comprobantes externos y consultar su
  validez oficial con OAuth. El resultado de emisión y el cobro se guardan separados.

Flujo: paciente → servicio/odontólogo → reserva → pago pendiente → cobro → confirmada →
atención → historial/ingresos. Apoyo (no módulos): inicio, servicios, nosotros, FAQ.

## Ejecución en Windows (Tomcat 9 + MySQL 8)

1. Requisitos: JDK 11+, Maven, MySQL 8.0.16+ (o 8.4), Tomcat 9.
2. Base de datos (con la app detenida; **respaldar antes**):
   `mysql -u root -p arquidentdb < database/000_base_nueva.sql` (solo base vacía nueva),
   `mysql -u root -p arquidentdb < database/001_erp.sql` (migración, no borra datos),
   `mysql -u root -p arquidentdb < database/002_demo.sql` (datos ficticios),
   `mysql -u root -p arquidentdb < database/003_emision_demo.sql` (emisiones y correlativos).
   Si viene del sistema anterior: `scripts/migrar-claves.ps1` convierte las claves a hash.
3. Configuración privada: copiar `config/ejemplo-windows.ps1` a `config/local.ps1`
   (ignorado por git) y completar `ARQ_DB_*`. `FD_DEMO_ENABLED=true` activa las
   boletas beta con el RUC de ejemplo; `FD_DEMO_SERIE=BA26`. `SUNAT_*` se necesita
   solo para la consulta oficial de validez de documentos de producción.
4. Compilar: `mvn package` → `target/arquident.war`. Copiar a `webapps/` de Tomcat y arrancar.
5. Abrir `http://localhost:8080/arquident/inicio`. Cuentas ficticias en `database/002_demo.sql`.

Arranque con configuración: `powershell -ExecutionPolicy Bypass -File scripts/start-tomcat.ps1`.
Para compilar y desplegar también: añadir `-Build` (tras detener con `scripts/stop-tomcat.ps1`).
Prueba de Caja paso a paso: [docs/PRUEBA_CAJA_SUNAT.md](docs/PRUEBA_CAJA_SUNAT.md).

## Justificación breve

- **Lenguaje**: Java/Servlets/JSP porque es la base existente; reescribir cambiaría el
  objeto de estudio y el costo de explicar algo nuevo al profesor.
- **IDE**: NetBeans 19 (Maven integrado, compatible con el proyecto original).
- **Arquitectura**: MVC + DAO por capas con servicios transaccionales (`ErpDAO`):
  las reglas (cruces, autorización, estados) viven en servicios probables con JUnit;
  los JSP solo muestran datos y los filtros aplican sesión/rol/CSRF.

## Limitaciones honestas y pendientes

- Emisión académica verificada en SUNAT beta mediante Facturación Directa. El RUC,
  certificado demo y modo 0 corresponden al ejemplo del proveedor; no emite en producción.
- Consulta oficial de validez: sin `SUNAT_*` guarda `SIN_CONFIGURAR`.
- Migración probada en esquema vacío, no sobre el dump con datos: respaldar y validar.
- Sin prueba de carga; sí se verificó doble cobro simultáneo con MySQL y transporte HTTP de prueba.
- Sin devoluciones de cobros ni emisión de producción; los envíos inciertos no se reintentan automáticamente.
- Ver `docs/CALIDAD.md` (matriz y pendientes), `docs/SUNAT.md`, `docs/DEMO.md`.
