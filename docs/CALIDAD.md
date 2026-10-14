# Matriz de calidad y resultados reales

## Versiones y relación entre normas (sin copiar textos sujetos a licencia)

- **ISO/IEC 9126-1:2001** (retirada): modelo de calidad con 6 características
  (funcionalidad, fiabilidad, usabilidad, eficiencia, mantenibilidad, portabilidad).
- **ISO/IEC 25010:2011** la reemplazó dentro de SQuaRE (8 características, añade seguridad
  y compatibilidad); **ISO/IEC 25010:2023** (2.ª edición, vigente) consolida
  **9 características**. Se usa 25010:2023 como referencia principal y se indica entre
  paréntesis la característica 9126 equivalente con fines didácticos.
- **ISO/IEC 25023:2016** (vigente; en revisión hacia ISO/IEC 25000-23) define medidas
  para evaluar la calidad según 25010, pero **no impone umbrales**: los objetivos
  (< 2 s, 100 %, 0 duplicados) son **objetivos del proyecto**, no valores ISO.
- No se afirma certificación ni conformidad formal: es una aplicación académica de los
  criterios a funciones concretas, con evidencia reproducible.

## Resultados ejecutados el 2026-10-14 (MySQL 8.4 en Docker, Tomcat 9.0.91, Windows 11)

| Norma y criterio | Función | Prueba | Métrica (objetivo del proyecto) | Resultado real | Evidencia |
|---|---|---|---|---|---|
| 25010 Funcionalidad–Completitud (←9126 Idoneidad) | Reservar en horario disponible | POST /citas con slot libre (HTTP) | 303 + cita `sin_pagar` y pago `pendiente` | OK: cita 7 + pago 6 creados juntos | Salida prueba-humo2c + `SELECT` DB |
| 25010 Funcionalidad–Corrección (←9126 Exactitud) | Cruces y disponibilidad | Reserva duplicada; hora 07:00; borde 09:30; iniciar día distinto | 409 / 400 / 0 cruces en borde / 400 | OK en los 4 casos | HTTP 409/400 + SQL `cruce=1, borde=0` |
| 25010 Fiabilidad–Tolerancia a fallos | Doble protección ante duplicados | Mismo (doctor,fecha,hora) directo en SQL + doble pago por cita | 0 citas duplicadas, 0 pagos duplicados | OK: `ERROR 1062` en `unica_cita_activa` y `pagos.id_cita` | Salida mysql |
| 25010 Fiabilidad–Recuperabilidad | Cancelar y re-reservar | Cancelar reserva sin cobro y reinsertar slot | Re-reserva permitida, cancelada no bloquea | OK (id 3 reinsertado) | Salida mysql |
| 25010 Seguridad–Autenticidad/Confidencialidad (nueva en 25010) | Autorización y CSRF | Paciente cobra (403), paciente ve otro paciente (403), POST sin token (403), claves PBKDF2 | 100 % bloqueos, 0 secretos en repo | OK: 403/403/403; 19/19 tests | HTTP + `mvn test` |
| 25010 Seguridad–Integridad | Consistencia cita↔pago | Cobro de caja: pago `pagado` + cita `confirmada` | Atomicidad 1/1, ingresos exactos | OK: `confirmada/pagado`, ingresos S/. 50.00 | DB + GET /caja |
| 25010 Eficiencia–Comportamiento temporal (←9126 Eficiencia) | Tiempos de operaciones principales | Medición local (segundo arranque, “warm”) | Objetivo: p95 < 2000 ms en red local | OK: login 118–127 ms; páginas 13–39 ms; POST 14–32 ms (primer arranque 195–417 ms por JIT/conexiones) | Tabla de tiempos del humo |
| 25010 Usabilidad–Operabilidad | Guion de 4 módulos | docs/DEMO.md ejecutable en ~10 min | Flujo sin atajos ni rutas ocultas | OK: flujo grabado en humo 1+2c | Scripts temp + este doc |
| 25023 (medida aplicada) | Validez tributaria vs cobro | Comprobante + “Consultar en SUNAT” sin credenciales | Resultado `SIN_CONFIGURAR` guardado, sin simulación | OK | `consultas_sunat.resultado=SIN_CONFIGURAR` |
| 25010 Mantenibilidad–Modularidad | Alcance reducido | 4 módulos + funciones de apoyo (FAQ, reportes en Caja) | 0 módulos extra, 0 frameworks nuevos | OK: rutas /pacientes /recursos /citas /caja | web.xml + header |

Tiempos medidos (ms, red local, base con 6 usuarios demo):
login 118–127 · GET citas/pacientes/recursos 13–39 · POST reservar/cobrar/iniciar/finalizar 14–32 ·
GET caja 18 (primer acceso 417 por arranque) · errores 400/403/409 en 1–19 ms.

## Pendiente (no ejecutado, sin resultados inventados)

1. Concurrencia real simultánea sobre el mismo slot (solo verificada la defensa en
   dos niveles: bloqueo `SELECT … FOR UPDATE` + restricción única).
2. Migración `001_erp.sql` sobre el dump original con datos (`arquidentdb.sql`):
   solo se ejecutó sobre esquema vacío; exige respaldo previo (el DDL confirma implícitamente).
3. `scripts/migrar-claves.ps1` contra claves reales heredadas (compila; sin BD legacy disponible).
4. Consulta SUNAT real (requiere `SUNAT_CLIENT_ID/SECRET/RUC` del contribuyente).
5. Rendimiento bajo carga (solo 1 usuario; sin prueba de estrés).
6. Pruebas de usabilidad con usuarios finales (solo guion ejecutado por el desarrollador).

## Verificación adicional — emisión automática (2026-10-14)

- `mvn package`: 31 pruebas, 0 fallos, 0 errores, 0 omitidas. Se configuró
  `ARQ_TEST_DB_URL/USER/PASSWORD` con el esquema separado `arquident_cpe_test`
  (migraciones 000,001,002,003 y datos demo) para ejecutar `CajaEmisionTest`.
- Concurrencia real contra MySQL: dos cobros simultáneos de la misma cita producen
  un cobro, un envío y un 409; el transporte externo de estas pruebas es falso.
- Timeout HTTP: conserva pago/cita confirmados y persiste `NO_CONFIRMADO`; un
  nuevo cobro no repite la llamada. Datos inválidos: rollback de pago y correlativo.
- Prueba real con Edge y el proveedor: cita 12 / pago 11, S/. 50.00,
  **BA26-100001**, HTTP 200, código 0, `ACEPTADO_DEMO`. PDF descargado,
  XML/PEN/50.00 y CDR beta/ResponseCode 0 comprobados.
- Segundo POST real de cobro: 409. CSRF inválido: 403. Sin errores JavaScript.

Esta prueba verifica consumo real de SUNAT beta a través de Facturación Directa.
La consulta OAuth oficial de producción continúa pendiente de sus credenciales.
Los tiempos anteriores de cobro se midieron antes de añadir la llamada externa.

## Verificación adicional — identidad visual y PDF (2026-10-14)

- `mvn package`: **33 pruebas**, 0 fallos, 0 errores, 0 omitidas, incluidas las cinco
  pruebas con MySQL aislado. Las dos nuevas pruebas de PDF comprueban sustitución
  de logo, conservación del texto/importes/QR y rechazo de una plantilla sin cabecera.
- Edge: logos públicos con HTTP 200 y MIME `image/png`; acceso, cabecera, pie y
  favicon actualizados. Iconos cargados correctamente; Caja sin bloques explicativos.
- Caja comprobada a 1440 px y 390 px; sin desbordamiento horizontal de la página
  ni errores JavaScript. Tablas con desplazamiento interno en móvil.
- PDF de **BA26-100002** accesible como administrador; PDF de **BA26-100001** accesible
  como secretaría. Usuario anónimo: 302 al acceso; odontólogo: 403; emisión inexistente:
  404; identificador inválido: 400.
- Comparación de los dos PDF reales antes/después: texto idéntico; renderizado a
  144 dpi con cambios exclusivamente en el logo. QR y resto del documento idénticos.
- Esta comprobación de branding solo consultó y descargó documentos existentes.
