package pe.edu.utp.service;

import java.sql.*;
import java.time.*;
import java.math.BigDecimal;
import java.util.*;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.security.*;

public class RecursoService {
    public static final List<String> DIAS = Arrays.asList("lunes", "martes", "miércoles", "jueves", "viernes", "sábado", "domingo");
    private final ErpDAO dao;
    public RecursoService(ErpDAO dao) { this.dao = dao; }
    public Map<String, Object> catalogos() throws SQLException {
        return dao.read(c -> {
            Map<String,Object> m = new HashMap<>();
            m.put("servicios", rows(c, "SELECT * FROM servicios ORDER BY nombre"));
            m.put("odontologos", rows(c, "SELECT id_usuario,nombre,correo,telefono,dni,direccion,fecha_nacimiento,activo FROM usuarios WHERE rol_id=3 ORDER BY nombre"));
            m.put("horarios", rows(c, "SELECT h.*,u.nombre FROM horarios_odontologos h JOIN usuarios u ON u.id_usuario=h.id_odontologo ORDER BY h.id_odontologo,h.dia_semana,h.hora_inicio"));
            m.put("asignaciones", rows(c, "SELECT a.*,u.nombre AS odontologo,s.nombre AS servicio FROM odontologo_servicios a JOIN usuarios u ON u.id_usuario=a.id_odontologo JOIN servicios s ON s.id_servicio=a.id_servicio ORDER BY u.nombre,s.nombre"));
            m.put("dias", DIAS); return m;
        });
    }
    public void odontologo(Usuario actor, Usuario p, boolean activo) throws SQLException {
        Permisos.exigir(actor, "administrador"); Validar.persona(p);
        String hash = null;
        if (p.getIdUsuario() == 0 || (p.getContrasena() != null && !p.getContrasena().isEmpty())) {
            Validar.clave(p.getContrasena()); hash = Passwords.hash(p.getContrasena());
        }
        final String passwordHash = hash;
        dao.transaction(c -> {
            if (p.getIdUsuario() == 0) {
                insert(c, "INSERT INTO usuarios(nombre,correo,contrasena,rol_id,dni,telefono,direccion,fecha_nacimiento,cuenta_validada,activo) VALUES(?,?,?,3,?,?,?,?,1,?)",
                        p.getNombre(),p.getCorreo(),passwordHash,p.getDni(),p.getTelefono(),p.getDireccion(),p.getFechaNacimiento(),activo);
            } else {
                doctor(c, p.getIdUsuario());
                if (!activo && one(c, "SELECT id_cita FROM citas WHERE id_odontologo=? AND fecha>=CURRENT_DATE AND estado NOT IN ('cancelada','finalizada') LIMIT 1", p.getIdUsuario()) != null)
                    throw new ReglaNegocio("El odontólogo tiene citas pendientes; atiéndalas o cancele las que no tengan cobro.");
                update(c, "UPDATE usuarios SET nombre=?,correo=?,dni=?,telefono=?,direccion=?,fecha_nacimiento=?,activo=? WHERE id_usuario=? AND rol_id=3",
                        p.getNombre(),p.getCorreo(),p.getDni(),p.getTelefono(),p.getDireccion(),p.getFechaNacimiento(),activo,p.getIdUsuario());
                if (passwordHash != null) update(c, "UPDATE usuarios SET contrasena=? WHERE id_usuario=?",passwordHash,p.getIdUsuario());
            }
            return null;
        });
    }
    public void servicio(Usuario actor, int id, String nombre, String descripcion, BigDecimal precio, int minutos, boolean activo, boolean previa) throws SQLException {
        Permisos.exigir(actor, "administrador");
        String n = Validar.texto(nombre,"Servicio",2,100), d = Validar.texto(descripcion,"Descripción",0,1000);
        BigDecimal importe = Validar.dinero(precio == null ? null : precio.toPlainString());
        if (minutos < 15 || minutos > 240 || minutos % 15 != 0) throw new ReglaNegocio("Duración: 15–240 minutos, múltiplo de 15.");
        dao.transaction(c -> {
            if (id == 0) insert(c, "INSERT INTO servicios(nombre,descripcion,precio,duracion_minutos,activo,requiere_consulta) VALUES(?,?,?,?,?,?)",n,d,importe,minutos,activo,previa);
            else {
                if (one(c,"SELECT id_servicio FROM servicios WHERE id_servicio=? FOR UPDATE",id)==null) throw new ReglaNegocio(404,"Servicio no encontrado.");
                if (!activo && one(c, "SELECT id_cita FROM citas WHERE id_servicio=? AND fecha>=CURRENT_DATE AND estado NOT IN ('cancelada','finalizada') LIMIT 1",id)!=null)
                    throw new ReglaNegocio("El servicio tiene citas pendientes.");
                update(c,"UPDATE servicios SET nombre=?,descripcion=?,precio=?,duracion_minutos=?,activo=?,requiere_consulta=? WHERE id_servicio=?",n,d,importe,minutos,activo,previa,id);
            }
            return null;
        });
    }
    static void doctor(Connection c, int id) throws SQLException {
        if (one(c,"SELECT id_usuario FROM usuarios WHERE id_usuario=? AND rol_id=3 FOR UPDATE",id)==null)
            throw new ReglaNegocio("Odontólogo inválido.");
    }
    public void horario(Usuario actor, int doctor, String dia, LocalTime inicio, LocalTime fin) throws SQLException {
        Permisos.exigir(actor,"administrador");
        if (!DIAS.contains(dia) || inicio == null || fin == null || !fin.isAfter(inicio)
                || inicio.getSecond()!=0 || fin.getSecond()!=0 || inicio.getMinute()%15!=0 || fin.getMinute()%15!=0)
            throw new ReglaNegocio("Horario inválido: mismo día e intervalos de 15 minutos.");
        dao.transaction(c -> {
            doctor(c,doctor);
            if (one(c,"SELECT id_horario FROM horarios_odontologos WHERE id_odontologo=? AND dia_semana=? AND hora_inicio<? AND hora_fin>? LIMIT 1",doctor,dia,fin,inicio)!=null)
                throw new ReglaNegocio("El horario laboral se cruza con otro existente.");
            insert(c,"INSERT INTO horarios_odontologos(id_odontologo,dia_semana,hora_inicio,hora_fin) VALUES(?,?,?,?)",doctor,dia,inicio,fin);
            return null;
        });
    }
    public void quitarHorario(Usuario actor, int id) throws SQLException {
        Permisos.exigir(actor,"administrador");
        dao.transaction(c -> {
            Map<String,Object> h = one(c,"SELECT * FROM horarios_odontologos WHERE id_horario=?",id);
            if (h==null) throw new ReglaNegocio(404,"Horario no encontrado.");
            doctor(c,number(h,"id_odontologo"));
            int dia = DIAS.indexOf(h.get("dia_semana"));
            if (one(c,"SELECT id_cita FROM citas WHERE id_odontologo=? AND WEEKDAY(fecha)=? AND fecha>=CURRENT_DATE AND estado NOT IN ('cancelada','finalizada') LIMIT 1", h.get("id_odontologo"),dia)!=null)
                throw new ReglaNegocio("Hay citas pendientes para ese día semanal. No se puede retirar el horario.");
            update(c,"DELETE FROM horarios_odontologos WHERE id_horario=?",id); return null;
        });
    }
    public void asignacion(Usuario actor, int doctor, int servicio, boolean agregar) throws SQLException {
        Permisos.exigir(actor,"administrador");
        dao.transaction(c -> {
            doctor(c,doctor);
            if (one(c,"SELECT id_servicio FROM servicios WHERE id_servicio=?",servicio)==null) throw new ReglaNegocio("Servicio inválido.");
            if (agregar) update(c,"INSERT INTO odontologo_servicios(id_odontologo,id_servicio) VALUES(?,?)",doctor,servicio);
            else {
                if (one(c,"SELECT id_cita FROM citas WHERE id_odontologo=? AND id_servicio=? AND fecha>=CURRENT_DATE AND estado NOT IN ('cancelada','finalizada') LIMIT 1",doctor,servicio)!=null)
                    throw new ReglaNegocio("La asignación tiene citas pendientes.");
                update(c,"DELETE FROM odontologo_servicios WHERE id_odontologo=? AND id_servicio=?",doctor,servicio);
            }
            return null;
        });
    }
}
