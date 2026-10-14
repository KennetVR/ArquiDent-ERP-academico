-- MySQL 8.0.16+ / 8.4. Ejecutar sobre la base existente, con la aplicación detenida.
-- No usar el dump original para actualizar: contiene DROP TABLE y eventos antiguos.
-- Respaldar antes. DDL de MySQL confirma implícitamente; no es una migración reversible.
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS erp_migraciones (
    version INT PRIMARY KEY, aplicada_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);
DROP PROCEDURE IF EXISTS migrar_arquident_v1;
DELIMITER $$
CREATE PROCEDURE migrar_arquident_v1()
BEGIN
  IF NOT EXISTS (SELECT 1 FROM erp_migraciones WHERE version=1) THEN
    -- Los eventos antiguos compiten con las transacciones y uno intenta eliminar citas.
    DROP EVENT IF EXISTS actualizar_pagos_expirados;
    DROP EVENT IF EXISTS actualizar_pagos_y_citas_expiradas;
    DROP EVENT IF EXISTS actualizar_pagos_y_eliminar_citas;
    DROP EVENT IF EXISTS evento_expirar_pagos_y_notificar;

    ALTER TABLE usuarios ADD activo BOOLEAN NOT NULL DEFAULT TRUE;
    ALTER TABLE servicios ADD duracion_minutos INT NOT NULL DEFAULT 45,
                          ADD activo BOOLEAN NOT NULL DEFAULT TRUE;
    ALTER TABLE citas ADD duracion_minutos INT NOT NULL DEFAULT 45,
                      ADD derivacion TEXT NULL,
                      ADD motivo TEXT NULL,
                      ADD erp_version INT NOT NULL DEFAULT 0,
                      ADD horario_activo TINYINT GENERATED ALWAYS AS
                        (CASE WHEN estado='cancelada' THEN NULL ELSE 1 END) STORED;
    ALTER TABLE citas DROP INDEX unica_cita_por_horario,
                      ADD UNIQUE KEY unica_cita_activa (id_odontologo,fecha,hora,horario_activo),
                      ADD KEY agenda_paciente (id_paciente,fecha,estado);
    ALTER TABLE pagos MODIFY estado_pago ENUM('pendiente','pagado','fallido','expirado','cancelado') DEFAULT 'pendiente',
                      ADD cobrado_por INT NULL,
                      ADD CONSTRAINT fk_pago_cajero FOREIGN KEY (cobrado_por) REFERENCES usuarios(id_usuario),
                      ADD KEY caja_fecha (estado_pago,fecha_pago);

    CREATE TABLE comprobantes (
      id_comprobante INT PRIMARY KEY AUTO_INCREMENT,
      id_pago INT NOT NULL UNIQUE,
      num_ruc CHAR(11) NOT NULL,
      cod_comp CHAR(2) NOT NULL,
      serie VARCHAR(4) NOT NULL,
      numero INT NOT NULL,
      fecha_emision DATE NOT NULL,
      monto DECIMAL(10,2) NOT NULL,
      registrado_por INT NOT NULL,
      registrado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      UNIQUE KEY documento_unico (num_ruc,cod_comp,serie,numero),
      FOREIGN KEY (id_pago) REFERENCES pagos(id_pago),
      FOREIGN KEY (registrado_por) REFERENCES usuarios(id_usuario)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    CREATE TABLE consultas_sunat (
      id_consulta INT PRIMARY KEY AUTO_INCREMENT,
      id_comprobante INT NOT NULL,
      consultado_por INT NOT NULL,
      consultado_en DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
      resultado VARCHAR(32) NOT NULL,
      http_status INT NULL,
      estado_cp VARCHAR(8) NULL,
      estado_ruc VARCHAR(8) NULL,
      condicion_domicilio VARCHAR(8) NULL,
      codigo_error VARCHAR(80) NULL,
      mensaje VARCHAR(1000) NOT NULL,
      observaciones TEXT NULL,
      FOREIGN KEY (id_comprobante) REFERENCES comprobantes(id_comprobante),
      FOREIGN KEY (consultado_por) REFERENCES usuarios(id_usuario),
      KEY ultima_consulta (id_comprobante,id_consulta)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
    INSERT INTO erp_migraciones(version) VALUES (1);
  END IF;
END$$
DELIMITER ;
CALL migrar_arquident_v1();
DROP PROCEDURE migrar_arquident_v1;

-- Se informa de inconsistencias heredadas sin inventar cobros ni reescribir el historial.
SELECT c.id_cita,c.estado,p.estado_pago,'Revisar cita/pago heredados' AS advertencia
FROM citas c LEFT JOIN pagos p ON p.id_cita=c.id_cita
WHERE p.id_pago IS NULL OR
      (c.estado IN ('confirmada','en_atencion','finalizada') AND p.estado_pago <> 'pagado');
SELECT id_servicio,nombre FROM servicios WHERE precio IS NULL OR precio <= 0;
-- La conversión de claves se ejecuta con scripts/migrar-claves.ps1 (PBKDF2 con sal individual).
