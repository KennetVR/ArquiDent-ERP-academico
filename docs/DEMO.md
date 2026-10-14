# Guion de demostración (10 minutos, datos ficticios de `database/002_demo.sql`)

Preparación: Tomcat con `arquident.war`, MySQL migrado (000→001→002→003), `config/local.ps1`
con la clave de base de datos y `FD_DEMO_ENABLED=true`. La emisión usa el RUC demo
y SUNAT beta vía Facturación Directa. Ver [PRUEBA_CAJA_SUNAT.md](PRUEBA_CAJA_SUNAT.md).

## 1. Módulo A — Pacientes (2 min)
1. Entrar como `caja.demo@arquident.edu`. Abrir **Pacientes**.
2. Registrar un paciente ficticio (DNI 8 dígitos, clave de 12+). Queda validado al crearse por caja.
3. Abrir su ficha: mostrar datos e historial vacío (sin citas aún).

## 2. Módulo B — Recursos (2 min, como `admin.demo@arquident.edu`)
1. Abrir **Recursos**: servicios con precio y duración, odontólogos, horarios, asignaciones.
2. Intentar agregar un horario que se cruce con otro del mismo odontólogo → error esperado.
3. Quitar una asignación con citas pendientes (si las hay) → bloqueado con mensaje.

## 3. Módulo C — Citas (3 min, como paciente `paci1.demo@arquident.edu`)
1. Abrir **Citas** → consultar disponibilidad (servicio + odontólogo asignado + fecha).
2. Reservar un horario libre → mensaje “genera un pago pendiente”.
3. Repetir el mismo horario → **409** (cruce). Probar una hora fuera del horario laboral → **400**.

## 4. Módulo D — Caja (2 min, como `caja.demo@arquident.edu`)
1. Abrir **Caja**: elegir Yape en el pendiente y pulsar **Cobrar y emitir**.
2. Mostrar que la cita pasa a **confirmada** y que los ingresos del rango suman el importe.
3. En **Comprobantes emitidos**, abrir **Detalle SUNAT** para mostrar el HTTP y código
   devueltos realmente por la API. Si acepta, abrir **PDF** (con logo ArquiDent), **XML** y **CDR**.
4. Explicar que el cobro y la emisión son estados separados; un fallo de API conserva
   el pago, muestra el error y no hace un segundo cobro. El formulario externo queda
   como opción para consultas oficiales con OAuth y no es necesario para esta demo.

## 5. Atención y cierre (1 min, como `odonto1.demo@arquident.edu`)
1. **Iniciar atención** (solo el día de la cita y con cobro registrado) → estado `en_atencion`.
2. **Finalizar** con observaciones y receta → `finalizada`.
3. Volver a **Pacientes** → la ficha muestra la cita, la atención y el pago.
