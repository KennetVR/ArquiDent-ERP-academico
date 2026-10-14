# Integración SUNAT — Emisión demo al cobrar y consulta oficial

## Emisión automática DEMO (implementada y probada)

Flujo: **Caja → Cobrar y emitir → POST real a Facturación Directa →
respuesta SUNAT beta → PDF/XML/CDR**. El formulario de comprobantes externos es
opcional y no interviene en este recorrido.

- Endpoint fijo: `https://facturaciondirecta.com/API_SUNAT/post.php`.
- `Content-Type: application/json; charset=UTF-8`. Bloques `empresa`, `cliente`,
  `venta`, `items`, según el ejemplo proporcionado. No se inventa autenticación Basic.
- RUC demo `20604051984`, `modo="0"`, usuario/clave de ejemplo `MODDATOS`.
- Siempre boleta `03`, serie B, DNI `codigo_tipo_entidad="1"` (RUC sería `6`).
- `moneda_id="1"` significa PEN; `2` produce USD (comprobado en el XML del proveedor).
- Pago al contado `forma_pago_id="1"`, unidad de servicio `ZZ`, código de odontología
  `85122001`, afectación demo `10`. El precio de Caja incluye IGV del 18% en esta demo:
  S/. 50.00 = base 42.37 + IGV 7.63; no se incrementa el importe del pago.

Configurar `FD_DEMO_ENABLED=true` y `FD_DEMO_SERIE=BA26` en `config/local.ps1`,
aplicar `database/003_emision_demo.sql` y reiniciar Tomcat para cargar el entorno.
Las variables `SUNAT_CLIENT_*` no se utilizan en la emisión demo.

### Persistencia y errores

`CajaService` confirma cita y pago y reserva el documento en una sola transacción.
`series_cpe_demo` guarda el correlativo por RUC/serie (empieza en 100001); el bloqueo
SQL impide reutilizarlo. `emisiones_cpe.id_pago` es único. Un doble cobro devuelve
409 y no vuelve a llamar a la API. El envío HTTP ocurre después del commit y sin
mantener conexiones ni bloqueos SQL. No se reenvía automáticamente tras timeout.

Contrato de respuesta comprobado:

```json
{"data":{"error":"false","respuesta_sunat_codigo":"0","respuesta_sunat_descripcion":"La Boleta numero BA26-100001, ha sido aceptada","ruta_pdf":"http://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/PDF/20604051984-03-BA26-100001.pdf","ruta_xml":"http://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/FIRMA/20604051984-03-BA26-100001.xml","ruta_cdr":"http://facturaciondirecta.com/API_SUNAT/files/facturacion_electronica/CDR/R-20604051984-03-BA26-100001.xml"}}
```

Solo `error=false` y código `0` se guardan como `ACEPTADO_DEMO` (etiqueta **Aceptado**
en Caja). HTTP 200 por sí solo no basta. Se guardan estado, HTTP, código SUNAT,
mensaje y enlaces del documento; los detalles se despliegan en **Detalle SUNAT**.
Los enlaces se validan contra el host y el nombre exacto del comprobante y se sirven
con HTTPS. No se muestra HTML arbitrario del proveedor ni se guarda el request con claves.

`RECHAZADO`, `ERROR_HTTP`, `RESPUESTA_INVALIDA` y `NO_CONFIRMADO` se muestran en Caja
sin revertir el pago. Si queda `ENVIANDO` tras una interrupción, se debe revisar ese
documento en el proveedor antes de reenviarlo. No se vuelve a cobrar la cita.

Prueba real 2026-10-14: cobro de S/. 50.00, cita 12 / pago 11, boleta **BA26-100001**,
HTTP **200**, código **0**, `ACEPTADO_DEMO`. PDF descargado; XML con moneda PEN e importe
50.00; CDR con `ResponseCode=0` y referencias beta. Es consumo real vía proveedor
hacia el entorno de pruebas, no un documento tributario de producción.

### PDF con identidad de ArquiDent

El botón **PDF** pasa por `GET /caja?accion=pdf&emision={id}`, disponible para
administrador y secretaría. `BoletaPdfService` obtiene la ruta del documento
aceptado en MySQL, verifica host y nombre exactos y descarga por HTTPS sin
redirecciones (máximo 5 MiB, conexión de 5 s y solicitud de 20 s).

`LogoBoletaPdf` utiliza Apache PDFBox para reemplazar la imagen de cabecera de la
plantilla por `images/arquident-logo.png`, respetando su proporción. Conserva los
datos del emisor, cliente, detalle, importes, texto y QR. No vuelve a emitir ni
registra otro cobro; también funciona con las boletas anteriores. XML y CDR
siguen siendo los archivos originales. El PDF se sirve sin caché desde la aplicación.

El logo completo se usa además en acceso, registro y pie de página; el isotipo
se usa en navegación y favicon. Ambos PNG están incluidos en el WAR.

## Consulta Integrada de Validez (oficial, independiente)

Fuente oficial: *Manual de Consulta Integrada de Comprobante de Pago por Servicio WEB*
(SUNAT, vía gob.pe: “Manual de consulta integrada de validez de Comprobantes de Pago”).
`SunatClient` implementa la consulta de validez de documentos previamente emitidos.

## Endpoints oficiales de consulta (fijos en `SunatClient.java`)

1. Token (POST, `x-www-form-urlencoded`):
   `https://api-seguridad.sunat.gob.pe/v1/clientesextranet/{client_id}/oauth2/token/`
   con `grant_type=client_credentials`, `scope=https://api.sunat.gob.pe/v1/contribuyente/contribuyentes`,
   `client_id`, `client_secret`. Responde `access_token`, `token_type`, `expires_in` (segundos).
2. Consulta (POST, JSON): `https://api.sunat.gob.pe/v1/contribuyente/contribuyentes/{RUC}/validarcomprobante`
   con cabecera `Authorization: Bearer {token}` y cuerpo
   `numRuc, codComp, numeroSerie, numero, fechaEmision (dd/mm/yyyy), monto`.
   Responde `success, message, data.estadoCp, data.estadoRuc, data.condDomiRuc, data.observaciones, errorCode`.

## Códigos del manual (se guardan y muestran tal cual)

- `estadoCp`: 0 NO EXISTE · 1 ACEPTADO · 2 ANULADO · 3 AUTORIZADO · 4 NO AUTORIZADO.
- `estadoRuc`: 00 ACTIVO · 01/02/03/10/11/22 (bajas, suspensión, inhabilitado).
- `condDomiRuc`: 00 HABIDO · 09 PENDIENTE · 11 POR VERIFICAR · 12 NO HABIDO · 20 NO HALLADO.
- `codComp` admitidos en este prototipo: `01` factura, `03` boleta.

Regla del prototipo: **un HTTP 200 con `success:true` NO es un cobro**; solo informa la
validez tributaria (`resultado` = ACEPTADO, ANULADO, …). Un error técnico
(`NO_DISPONIBLE`, `TIEMPO_AGOTADO`, `RESPUESTA_INVALIDA`, …) **no determina la validez**.
El cobro se demuestra únicamente con el pago `pagado` en Caja.

## Configuración privada (sin credenciales en código, JSP, repo ni registros)

Variables de entorno del servidor (ver `config/ejemplo-windows.ps1`):

- `SUNAT_CLIENT_ID`, `SUNAT_CLIENT_SECRET` — credenciales creadas por única vez en
  SUNAT Operaciones en Línea (SOL): Empresas / Comprobantes de pago / Consulta de
  Validez / Credenciales de API SUNAT (nombre y URL de la aplicación).
- `SUNAT_RUC_CONSULTA` — RUC habilitado que realiza la consulta (va en la URL).

Sin estas variables, cada intento de consulta informa y guarda `SIN_CONFIGURAR`.
Esta consulta oficial no se sustituye por la emisión beta.

## Comportamiento implementado

- Token en memoria del backend con vencimiento (`expires_in` menos margen de 30 s o mitad),
  un solo reintento ante 401, time-outs de 5 s (conexión) y 15 s (lectura), sin redirecciones.
- Cada intento (éxito o error) se guarda en `consultas_sunat` con fecha, quién consultó,
  `resultado`, `http_status`, códigos SUNAT y mensaje. El estado del cobro (`pagos`)
  y el estado tributario (`consultas_sunat`) permanecen separados.
- El cuerpo HTTP completo y los secretos nunca se persisten ni se registran.

## Pendiente (requiere credenciales reales del contribuyente)

Consulta real contra SUNAT (aceptado/anulado/observado, 401 con secreto inválido,
429, token vencido). Sin credenciales no puede ejecutarse; la matriz de calidad lo
marca como pendiente sin inventar resultados.
