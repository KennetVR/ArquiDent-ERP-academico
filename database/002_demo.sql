-- Demostración con datos FICTICIOS. Ejecutar después de 001_erp.sql, con la app detenida:
--   mysql -h 127.0.0.1 -P 33316 -u root -p arquidentdb < database/002_demo.sql
-- Re-ejecutable: usa INSERT IGNORE / NOT EXISTS (no duplica usuarios, servicios ni asignaciones).
-- Cuentas ficticias (cámbielas tras la demo):
--   admin.demo@arquident.edu      / AdminDemo-2026-01      (administrador)
--   caja.demo@arquident.edu       / CajaDemo-2026-02       (secretaría/caja)
--   odonto1.demo@arquident.edu    / OdontoDemo-2026-03     (odontólogo)
--   odonto2.demo@arquident.edu    / OdontoDemo-2026-04     (odontólogo)
--   paci1.demo@arquident.edu      / PacienteDemo-2026-05   (paciente)
--   paci2.demo@arquident.edu      / PacienteDemo-2026-06   (paciente)
-- Las claves están en PBKDF2-HMAC-SHA256 (600 000 iteraciones, sal individual).
SET NAMES utf8mb4;

INSERT IGNORE INTO servicios (id_servicio,nombre,descripcion,precio,duracion_minutos,activo,requiere_consulta) VALUES
 (1,'Consulta General','Evaluación completa y plan de tratamiento',50.00,30,1,0),
 (2,'Limpieza Dental','Profilaxis profesional',80.00,45,1,0),
 (3,'Endodoncia','Tratamiento de conducto; requiere atención previa',350.00,90,1,1);

INSERT IGNORE INTO usuarios (nombre,correo,contrasena,rol_id,telefono,direccion,fecha_nacimiento,dni,cuenta_validada,activo) VALUES
 ('Admin Demo','admin.demo@arquident.edu','pbkdf2-sha256$600000$PbnC2zyxlyt8uvg5y6X8VA==$uxEi+5Vga6aU6F28AU6w52I2z+yGIN/IneAYiiAJqi4=',4,'900000001','Av. Demo 100, Lima','1985-01-01','40000001',1,1),
 ('Caja Demo','caja.demo@arquident.edu','pbkdf2-sha256$600000$9oHajVm0miEyZOUNUhem0A==$ByHzNukVcPVg95e5g6J6It20JlsZ+Gmakt9K99jWmjM=',2,'900000002','Av. Demo 100, Lima','1990-02-02','40000002',1,1),
 ('Odonto Uno','odonto1.demo@arquident.edu','pbkdf2-sha256$600000$GXSgGK76K7zL8vBdgCHNnw==$yq4lL++CgpiYVMheUh8dzFHRihtBwHSinvOFllkcwCM=',3,'900000003','Av. Demo 200, Lima','1988-03-03','40000003',1,1),
 ('Odonto Dos','odonto2.demo@arquident.edu','pbkdf2-sha256$600000$oV7jbdj+KSAbjxHrnAbSMw==$Pdv6zTf/u4u8I4vtGAN9SRlKRyQMel4Err5LD5wkqys=',3,'900000004','Av. Demo 300, Lima','1989-04-04','40000004',1,1),
 ('Paciente Uno','paci1.demo@arquident.edu','pbkdf2-sha256$600000$tEKeyls99l6Ph8aWQy5TQA==$CPzMwwofDoBcjUltCCAst3BTuqhg+/2Ei25TkBPUiw4=',1,'900000005','Jr. Demo 400, Lima','1995-05-05','40000005',1,1),
 ('Paciente Dos','paci2.demo@arquident.edu','pbkdf2-sha256$600000$5womuZP2+xf1sTFB2sCGYg==$MvyRa9cpeKE9N8H2qfYQY1CW7JOIKUBMVHIfWZJWNN8=',1,'900000006','Jr. Demo 500, Lima','1996-06-06','40000006',1,1);

SELECT id_usuario INTO @o1 FROM usuarios WHERE correo='odonto1.demo@arquident.edu';
SELECT id_usuario INTO @o2 FROM usuarios WHERE correo='odonto2.demo@arquident.edu';

INSERT INTO horarios_odontologos (id_odontologo,dia_semana,hora_inicio,hora_fin)
SELECT @o1,'lunes','08:00','13:00' WHERE NOT EXISTS (SELECT 1 FROM horarios_odontologos WHERE id_odontologo=@o1 AND dia_semana='lunes');
INSERT INTO horarios_odontologos (id_odontologo,dia_semana,hora_inicio,hora_fin)
SELECT @o1,'miércoles','08:00','13:00' WHERE NOT EXISTS (SELECT 1 FROM horarios_odontologos WHERE id_odontologo=@o1 AND dia_semana='miércoles');
INSERT INTO horarios_odontologos (id_odontologo,dia_semana,hora_inicio,hora_fin)
SELECT @o2,'martes','09:00','14:00' WHERE NOT EXISTS (SELECT 1 FROM horarios_odontologos WHERE id_odontologo=@o2 AND dia_semana='martes');
INSERT INTO horarios_odontologos (id_odontologo,dia_semana,hora_inicio,hora_fin)
SELECT @o2,'jueves','09:00','14:00' WHERE NOT EXISTS (SELECT 1 FROM horarios_odontologos WHERE id_odontologo=@o2 AND dia_semana='jueves');

INSERT IGNORE INTO odontologo_servicios (id_odontologo,id_servicio) VALUES (@o1,1),(@o1,2),(@o2,1),(@o2,3);

INSERT INTO faq (pregunta,respuesta)
SELECT '¿Cómo reservo una cita?','Ingrese a Citas, consulte la disponibilidad real de un servicio con su odontólogo asignado y confirme. La reserva genera un pago pendiente que caja registra.' WHERE NOT EXISTS (SELECT 1 FROM faq WHERE pregunta='¿Cómo reservo una cita?');
INSERT INTO faq (pregunta,respuesta)
SELECT '¿Qué significa el resultado de SUNAT?','Es solo la validez tributaria del documento (aceptado, anulado, etc.). No acredita que el dinero haya sido recibido; eso lo demuestra el cobro registrado en Caja.' WHERE NOT EXISTS (SELECT 1 FROM faq WHERE pregunta='¿Qué significa el resultado de SUNAT?');
