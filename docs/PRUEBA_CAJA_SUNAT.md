# Probar el cobro con emisión automática

## Prueba rápida en este equipo

1. Abrir <http://127.0.0.1:8080/arquident/login>.
2. Ingresar como `caja.demo@arquident.edu` / `CajaDemo-2026-02`
   (también funciona `admin.demo@arquident.edu` / `AdminDemo-2026-01`).
3. Ir a **Caja y comprobantes** (entorno **Pruebas**).
4. En **Pagos pendientes**, elegir un método (por ejemplo, `yape`).
5. Pulsar **Cobrar y emitir** una sola vez y esperar la respuesta.
6. En **Comprobantes emitidos**, revisar el estado **Aceptado**. Abrir **Detalle SUNAT**
   para ver HTTP `200` y código `0`; el estado guardado es `ACEPTADO_DEMO`.
7. Abrir **PDF**: muestra el logo de ArquiDent, también en boletas ya emitidas.
   Los botones **XML/CDR** abren los documentos originales del proveedor.

No hay que completar manualmente ID pago, RUC, serie, número, fecha ni monto.
El sistema obtiene el paciente/servicio del pago y genera el correlativo en MySQL.
El importe en soles ya incluye IGV. El formulario de documentos externos es opcional.

La cita #11 / pago #10 ya figura cobrada con la boleta **BA26-100002**.
Para repetir la demostración se reserva otra cita.

## Si no hay pagos pendientes

1. Ir a **Citas → Consultar disponibilidad real**.
2. Seleccionar `Paciente Uno` o `Paciente Dos`, `Consulta General` y un odontólogo.
3. Elegir una fecha futura con horario: Odonto Uno, lunes/miércoles; Odonto Dos,
   martes/jueves (horarios originales de los datos demo).
4. Pulsar **Ver horarios disponibles**. En el formulario de reserva se completan
   paciente, doctor, servicio y fecha. Elegir una hora libre y pulsar **Reservar cita**.
5. Volver a **Caja**. Ampliar el rango de fechas si la reserva no aparece.
6. Elegir el método y cobrar con emisión demo.

## Evidencia real de la verificación

- Navegador Edge: reserva → cobro → redirección a Caja → documentos.
- Cita #12, pago #11, boleta **BA26-100001**, S/. **50.00**.
- API real: **HTTP 200 / código SUNAT 0 / ACEPTADO_DEMO**.
- PDF descargable; XML con `PEN` y `PayableAmount=50.00`;
  CDR con `ResponseCode=0` del entorno beta.
- Segundo POST de cobro: **409**, sin otra emisión. CSRF inválido: **403**.
- Maven: **33 pruebas aprobadas**, incluidas 5 con MySQL aislado y HTTP de prueba
   (concurrencia, correlativo persistente, rollback, timeout y fallo al guardar la respuesta).
- Dos pruebas de PDF verifican que el cambio de logo conserva texto, importes y QR.

La API se consume realmente mediante Facturación Directa hacia SUNAT **beta**.
La aceptación demo no equivale a emitir un comprobante válido en producción.

## Errores que pueden mostrarse

Si aparece `RECHAZADO`, revisar el código y mensaje del proveedor. `NO_CONFIRMADO`
significa que no se obtuvo una respuesta concluyente (por ejemplo, timeout).
El pago sigue registrado; **no cobrar otra vez**. Revisar el mismo serie/número
en el proveedor antes de intentar un reenvío. La aplicación no reenvía automáticamente.

Si el botón indica **Registrar cobro** en lugar de **Cobrar y emitir**, revisar `config/local.ps1`:

```powershell
$env:FD_DEMO_ENABLED = 'true'
$env:FD_DEMO_SERIE = 'BA26'
```

Aplicar `database/003_emision_demo.sql` una vez y reiniciar con los scripts de Tomcat.
Para la emisión demo no se requieren `SUNAT_CLIENT_ID` ni `SUNAT_CLIENT_SECRET`.
