-- SOLO para una base VACÍA de demostración. Elegir la base con mysql -D nombre.
-- Reproduce las tablas del proyecto original, sin usuarios ni documentos reales.
SET NAMES utf8mb4;
CREATE TABLE roles (id_rol INT PRIMARY KEY, nombre_rol VARCHAR(50) NOT NULL UNIQUE) ENGINE=InnoDB;
INSERT INTO roles VALUES (1,'paciente'),(2,'secretaria'),(3,'odontologo'),(4,'administrador');
CREATE TABLE usuarios (
 id_usuario INT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL,
 correo VARCHAR(100) NOT NULL UNIQUE, contrasena VARCHAR(255) NOT NULL,
 rol_id INT NOT NULL, telefono VARCHAR(15), direccion TEXT, fecha_nacimiento DATE,
 fecha_registro DATETIME DEFAULT CURRENT_TIMESTAMP, dni VARCHAR(8) UNIQUE,
 foto_dni_frontal VARCHAR(255), foto_dni_reverso VARCHAR(255), cuenta_validada BOOLEAN DEFAULT FALSE,
 fecha_solicitud_registro DATETIME DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY (rol_id) REFERENCES roles(id_rol)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE servicios (
 id_servicio INT PRIMARY KEY AUTO_INCREMENT, nombre VARCHAR(100) NOT NULL,
 descripcion TEXT, precio DECIMAL(10,2), requiere_consulta BOOLEAN DEFAULT FALSE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE horarios_odontologos (
 id_horario INT PRIMARY KEY AUTO_INCREMENT, id_odontologo INT NOT NULL,
 dia_semana ENUM('lunes','martes','miércoles','jueves','viernes','sábado','domingo') NOT NULL,
 hora_inicio TIME NOT NULL, hora_fin TIME NOT NULL,
 FOREIGN KEY(id_odontologo) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE odontologo_servicios (
 id_odontologo INT NOT NULL, id_servicio INT NOT NULL, PRIMARY KEY(id_odontologo,id_servicio),
 FOREIGN KEY(id_odontologo) REFERENCES usuarios(id_usuario), FOREIGN KEY(id_servicio) REFERENCES servicios(id_servicio)
) ENGINE=InnoDB;
CREATE TABLE citas (
 id_cita INT PRIMARY KEY AUTO_INCREMENT, id_paciente INT NOT NULL, id_odontologo INT NOT NULL,
 id_servicio INT NOT NULL, fecha DATE NOT NULL, hora TIME NOT NULL,
 estado ENUM('reservada','sin_pagar','confirmada','en_atencion','finalizada','cancelada','derivada_sin_pagar') DEFAULT 'sin_pagar',
 hora_inicio_real DATETIME, hora_fin_real DATETIME, observaciones TEXT, receta TEXT,
 fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
 UNIQUE KEY unica_cita_por_horario(id_odontologo,fecha,hora),
 FOREIGN KEY(id_paciente) REFERENCES usuarios(id_usuario),
 FOREIGN KEY(id_odontologo) REFERENCES usuarios(id_usuario), FOREIGN KEY(id_servicio) REFERENCES servicios(id_servicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE pagos (
 id_pago INT PRIMARY KEY AUTO_INCREMENT, id_cita INT NOT NULL UNIQUE,
 monto DECIMAL(10,2) NOT NULL, metodo_pago VARCHAR(50),
 estado_pago ENUM('pendiente','pagado','fallido','expirado') DEFAULT 'pendiente',
 fecha_pago DATETIME, fecha_limite_pago DATETIME, FOREIGN KEY(id_cita) REFERENCES citas(id_cita)
) ENGINE=InnoDB;
CREATE TABLE faq (
 id_pregunta INT PRIMARY KEY AUTO_INCREMENT, pregunta TEXT NOT NULL, respuesta TEXT NOT NULL,
 id_usuario_creador INT, fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
 fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
 FOREIGN KEY(id_usuario_creador) REFERENCES usuarios(id_usuario) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE notificaciones (
 id_notificacion INT PRIMARY KEY AUTO_INCREMENT, id_usuario INT NOT NULL, mensaje TEXT NOT NULL,
 leido BOOLEAN DEFAULT FALSE, fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
 FOREIGN KEY(id_usuario) REFERENCES usuarios(id_usuario)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
