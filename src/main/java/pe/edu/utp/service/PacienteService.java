package pe.edu.utp.service;

import java.sql.*;
import java.util.*;
import pe.edu.utp.dao.ErpDAO;
import static pe.edu.utp.dao.ErpDAO.*;
import pe.edu.utp.entity.Usuario;
import pe.edu.utp.security.*;

public class PacienteService {
    private final ErpDAO dao;
    public PacienteService(ErpDAO dao) { this.dao = dao; }
    private static final String FIELDS = "u.id_usuario,u.nombre,u.correo,u.dni,u.telefono,u.direccion,u.fecha_nacimiento,u.cuenta_validada,u.activo,u.rol_id";

    public Usuario autenticar(String correo, String clave) throws SQLException {
        if (correo == null || correo.length() > 100 || clave == null || clave.length() > 128) return null;
        return dao.read(c -> {
            Map<String, Object> r = one(c, "SELECT u.*,r.nombre_rol FROM usuarios u JOIN roles r ON r.id_rol=u.rol_id WHERE correo=? AND activo=1 AND cuenta_validada=1", correo.trim());
            if (r == null || !Passwords.verify(clave, (String) r.get("contrasena"))) return null;
            return actor(r);
        });
    }
    public Usuario vigente(int id) throws SQLException {
        return dao.read(c -> {
            Map<String, Object> r = one(c, "SELECT u.id_usuario,u.nombre,r.nombre_rol FROM usuarios u JOIN roles r ON r.id_rol=u.rol_id WHERE u.id_usuario=? AND activo=1 AND cuenta_validada=1", id);
            return r == null ? null : actor(r);
        });
    }
    private Usuario actor(Map<String, Object> r) {
        Usuario u = new Usuario(); u.setIdUsuario(number(r, "id_usuario"));
        u.setNombre((String) r.get("nombre")); u.setNombreRol((String) r.get("nombre_rol")); return u;
    }
    public List<Map<String, Object>> listar(Usuario actor) throws SQLException {
        Permisos.exigir(actor, "paciente", "odontologo", "secretaria", "administrador");
        return dao.read(c -> {
            String where = ""; Object[] args = {};
            if (Permisos.rol(actor, "paciente")) { where = " AND u.id_usuario=?"; args = new Object[]{actor.getIdUsuario()}; }
            if (Permisos.rol(actor, "odontologo")) { where = " AND EXISTS(SELECT 1 FROM citas a WHERE a.id_paciente=u.id_usuario AND a.id_odontologo=?)"; args = new Object[]{actor.getIdUsuario()}; }
            return rows(c, "SELECT " + FIELDS + " FROM usuarios u WHERE u.rol_id=1" + where + " ORDER BY u.nombre", args);
        });
    }
    public Map<String, Object> obtener(Usuario actor, int id) throws SQLException {
        return dao.read(c -> { acceso(c, actor, id); return one(c, "SELECT " + FIELDS + " FROM usuarios u WHERE id_usuario=?", id); });
    }
    public static void acceso(Connection c, Usuario actor, int id) throws SQLException {
        Permisos.exigir(actor, "paciente", "odontologo", "secretaria", "administrador");
        Map<String,Object> p = one(c, "SELECT id_usuario FROM usuarios WHERE id_usuario=? AND rol_id=1", id);
        if (p == null) throw new ReglaNegocio(404, "Paciente no encontrado.");
        if (Permisos.caja(actor) || (Permisos.rol(actor, "paciente") && actor.getIdUsuario() == id)) return;
        if (Permisos.rol(actor, "odontologo") && one(c, "SELECT id_cita FROM citas WHERE id_paciente=? AND id_odontologo=? LIMIT 1", id, actor.getIdUsuario()) != null) return;
        throw new ReglaNegocio(403, "No tiene relación autorizada con este paciente.");
    }
    public int registrar(Usuario actor, Usuario p) throws SQLException {
        if (actor != null) Permisos.exigir(actor, "secretaria", "administrador");
        Validar.persona(p); Validar.clave(p.getContrasena());
        String hash = Passwords.hash(p.getContrasena());
        return dao.transaction(c -> insert(c, "INSERT INTO usuarios(nombre,correo,contrasena,rol_id,telefono,direccion,fecha_nacimiento,dni,cuenta_validada) VALUES(?,?,?,1,?,?,?,?,?)",
                p.getNombre(), p.getCorreo(), hash, p.getTelefono(), p.getDireccion(), p.getFechaNacimiento(), p.getDni(), actor != null));
    }
    public void actualizar(Usuario actor, Usuario p, boolean validado) throws SQLException {
        Permisos.exigir(actor, "paciente", "secretaria", "administrador"); Validar.persona(p);
        dao.transaction(c -> {
            acceso(c, actor, p.getIdUsuario());
            one(c, "SELECT id_usuario FROM usuarios WHERE id_usuario=? FOR UPDATE", p.getIdUsuario());
            update(c, "UPDATE usuarios SET nombre=?,correo=?,telefono=?,direccion=?,fecha_nacimiento=?,dni=? WHERE id_usuario=? AND rol_id=1",
                    p.getNombre(), p.getCorreo(), p.getTelefono(), p.getDireccion(), p.getFechaNacimiento(), p.getDni(), p.getIdUsuario());
            if (Permisos.caja(actor)) update(c, "UPDATE usuarios SET cuenta_validada=? WHERE id_usuario=?", validado, p.getIdUsuario());
            return null;
        });
    }
}
