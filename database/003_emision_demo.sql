-- Emisión automática al cobrar: SUNAT beta vía Facturación Directa.
-- Ejecutar después de 001_erp.sql. No modifica pagos ni comprobantes existentes.
SET NAMES utf8mb4;
CREATE TABLE IF NOT EXISTS series_cpe_demo (
  num_ruc CHAR(11) NOT NULL,
  serie CHAR(4) NOT NULL,
  ultimo_numero INT NOT NULL,
  PRIMARY KEY (num_ruc,serie)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS emisiones_cpe (
  id_emision INT PRIMARY KEY AUTO_INCREMENT,
  id_pago INT NOT NULL UNIQUE,
  num_ruc CHAR(11) NOT NULL,
  cod_comp CHAR(2) NOT NULL DEFAULT '03',
  serie CHAR(4) NOT NULL,
  numero INT NOT NULL,
  creado_en DATETIME NOT NULL,
  respondido_en DATETIME NULL,
  cliente_nombre VARCHAR(100) NOT NULL,
  cliente_dni CHAR(8) NOT NULL,
  cliente_direccion VARCHAR(500) NOT NULL,
  servicio VARCHAR(100) NOT NULL,
  monto DECIMAL(10,2) NOT NULL,
  gravada DECIMAL(10,2) NOT NULL,
  igv DECIMAL(10,2) NOT NULL,
  estado VARCHAR(32) NOT NULL DEFAULT 'ENVIANDO',
  http_status INT NULL,
  codigo_sunat VARCHAR(80) NULL,
  mensaje VARCHAR(1000) NOT NULL DEFAULT 'Envío iniciado. Si este estado persiste, revise el proveedor antes de reenviar.',
  ruta_pdf VARCHAR(1000) NULL,
  ruta_xml VARCHAR(1000) NULL,
  ruta_cdr VARCHAR(1000) NULL,
  codigo_hash VARCHAR(200) NULL,
  UNIQUE KEY emision_documento (num_ruc,cod_comp,serie,numero),
  KEY emision_fecha (creado_en),
  FOREIGN KEY (id_pago) REFERENCES pagos(id_pago)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT IGNORE INTO erp_migraciones(version) VALUES (3);
