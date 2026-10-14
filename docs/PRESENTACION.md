# Presentación — guía rápida

## Antes de salir

1. Encender Docker Desktop.
2. Hacer doble clic en **`INICIAR-DEMO.cmd`** dentro del proyecto.
3. Comprobar que termina con **ArquiDent listo para la presentación**.
4. Abrir <http://127.0.0.1:8080/arquident/login> y entrar como administrador.
5. Ejecutar `powershell -ExecutionPolicy Bypass -File scripts/backup-db.ps1`.
6. Llevar el proyecto, el SQL de `backups/` y el WAR de la versión publicada.

El arranque reutiliza los datos del equipo. No es necesario volver a compilar ni
descargar dependencias para abrir la versión preparada. La interfaz, fuentes y
logo están incluidos localmente. Para demostrar una **nueva boleta SUNAT beta**,
mantener conexión a Internet.

## Recorrido sugerido (8–10 minutos)

### 1. Presentar el sistema

“ArquiDent integra pacientes, recursos, citas y caja usando Java, Servlets, JSP
y MySQL. Las reglas de negocio están en servicios transaccionales y el acceso
a datos usa DAO”. Mostrar [el diagrama](ARQUITECTURA.md).

### 2. Recursos y disponibilidad

Entrar con `admin.demo@arquident.edu` / `AdminDemo-2026-01`.
Mostrar un servicio con precio/duración y los horarios del odontólogo.

- **Odonto Uno:** lunes y miércoles, 08:00–13:00; Consulta General y Limpieza.
- **Odonto Dos:** martes y jueves, 09:00–14:00; Consulta General y Endodoncia.
- Para **jueves 15/10/2026**, elegir **Odonto Dos + Consulta General**.
- Si la exposición ocurre fuera de esa jornada, el administrador puede añadir
  un horario compatible antes de consultar disponibilidad.

### 3. Reservar como paciente

En una ventana privada, entrar con `paci1.demo@arquident.edu` /
`PacienteDemo-2026-05`. La pestaña **Pacientes** no se muestra.

En **Citas**, consultar disponibilidad y reservar una hora libre.
Mostrar la cita **sin pagar**. Para demostrar atención en la misma sesión,
reservar para el día de la presentación; el sistema solo permite iniciar
atención en la fecha de la cita.

### 4. Cobrar y emitir

Volver a la ventana de administrador o entrar como `caja.demo@arquident.edu` /
`CajaDemo-2026-02`.

1. Abrir **Caja** y ubicar el paciente en **Pagos pendientes**.
2. Elegir Yape, efectivo, tarjeta o Plin; pulsar **Cobrar y emitir** una sola vez.
3. Mostrar el ingreso y el cobro registrado.
4. En **Comprobantes emitidos**, abrir **Detalle SUNAT**: HTTP, código y respuesta real.
5. Si está aceptado, abrir **PDF** con logo ArquiDent, **XML** y **CDR**.

“La llamada HTTP se realiza después de confirmar el pago. Un fallo externo
no deshace el cobro ni genera un segundo envío automático”.

Las boletas **BA26-100001** y **BA26-100002** ya verificadas están disponibles en
la base de este equipo; una instalación nueva solo incluye los datos iniciales.

### 5. Atención e historial

Ingresar como el odontólogo de la reserva; para Odonto Dos:
`odonto2.demo@arquident.edu` / `OdontoDemo-2026-04`.
Iniciar atención de la cita confirmada de hoy, registrar observaciones/receta
y finalizar. Volver al historial desde un rol autorizado.

## Si se interrumpe la conexión

Los módulos y el diseño funcionan localmente mientras MySQL/Tomcat estén activos.
La boleta nueva depende del proveedor; mostrar su estado real y el cobro guardado.
Puede mostrarse una boleta descargada previamente como evidencia de una ejecución
anterior. No presentar una respuesta guardada como si fuera una nueva emisión.

## Datos para explicar las pruebas

- 33 pruebas automatizadas: 28 autónomas y 5 con MySQL aislado.
- Doble cobro concurrente: un pago y un envío; el segundo intento recibe 409.
- CSRF y permisos por rol comprobados.
- PDF: texto, importes y QR conservados al reemplazar el logo.
- [Evidencias técnicas](CALIDAD.md) y [detalle de SUNAT](SUNAT.md).

Si aparece un problema de arranque, seguir [Solución de problemas](DESPLIEGUE.md#6-solución-de-problemas).
