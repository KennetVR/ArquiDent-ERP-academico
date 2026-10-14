package pe.edu.utp.security;

import pe.edu.utp.entity.Usuario;
import pe.edu.utp.service.ReglaNegocio;

public final class Permisos {
    private Permisos() { }
    public static boolean rol(Usuario u, String... roles) {
        if (u == null) return false;
        for (String r : roles) if (r.equals(u.getNombreRol())) return true;
        return false;
    }
    public static boolean caja(Usuario u) { return rol(u, "secretaria", "administrador"); }
    public static void exigir(Usuario u, String... roles) {
        if (!rol(u, roles)) throw new ReglaNegocio(403, "No tiene permiso para esta operación.");
    }
    public static void cita(Usuario u, int paciente, int odontologo) {
        if (caja(u) || (rol(u, "paciente") && u.getIdUsuario() == paciente)
                || (rol(u, "odontologo") && u.getIdUsuario() == odontologo)) return;
        throw new ReglaNegocio(403, "No tiene acceso a esta cita.");
    }
}
